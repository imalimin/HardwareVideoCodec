/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/AudioRecorder.h"

void bufferDequeueCallback(SLAndroidSimpleBufferQueueItf slBufferQueueItf, void *context) {
    AudioRecorder *recorder = static_cast<AudioRecorder *>(context);
    recorder->bufferDequeue(slBufferQueueItf);
}

void AudioRecorder::bufferDequeue(SLAndroidSimpleBufferQueueItf slBufferQueueItf) {
    LOGE("AudioRecorder...");
    if (buffer) {
        if (pcmFile) {
            fwrite(buffer->ptr, 1, getBufferByteSize(), pcmFile);
        }
        recycler->offer(buffer);
        buffer = nullptr;
    }
    buffer = recycler->takeCache();
    if (buffer) {
        (*slBufferQueueItf)->Enqueue(bufferQueueItf, buffer->ptr, getBufferByteSize());
    }
}

AudioRecorder::AudioRecorder(uint16_t channels,
                             uint32_t sampleRate,
                             uint16_t format,
                             uint32_t samplesPerBuffer) : SLAudioDevice(channels,
                                                                        sampleRate,
                                                                        format,
                                                                        samplesPerBuffer) {
    initialize(nullptr);
}

AudioRecorder::AudioRecorder(SLEngine *engine,
                             uint16_t channels,
                             uint32_t sampleRate,
                             uint16_t format,
                             uint32_t samplesPerBuffer) : SLAudioDevice(channels,
                                                                        sampleRate,
                                                                        format,
                                                                        samplesPerBuffer) {
    initialize(engine);
}

void AudioRecorder::initialize(SLEngine *engine) {
    pcmFile = fopen("/sdcard/pcm_tmp.pcm", "w");
    this->engine = engine;
    LOGI("Create AudioRecorder, channels=%d, sampleHz=%d",
         this->channels,
         this->sampleRate);
    uint32_t bufSize = getBufferByteSize();
    this->recycler = new RecyclerBlockQueue<ObjectBox>(16, [bufSize] {
        uint8_t *buf = new uint8_t[bufSize];
        memset(buf, 0, bufSize);
        return new ObjectBox(buf);
    });
    HwResult ret = this->createEngine();
    if (Hw::SUCCESS != ret) {
        LOGE("AudioRecorder start failed");
    }

}

AudioRecorder::~AudioRecorder() {
    LOGI("~AudioRecorder");
    stop();
}

HwResult AudioRecorder::start() {
    (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
    (*bufferQueueItf)->Clear(bufferQueueItf);
    bufferDequeue(bufferQueueItf);
    SLresult result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder SetRecordState start failed!");
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

void AudioRecorder::stop() {
    if (recordItf) {
        SLresult result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
        if (SL_RESULT_SUCCESS != result) {
            LOGE("Recorder SetRecordState stop failed!");
        }
    }
    if (pcmFile) {
        fclose(pcmFile);
        pcmFile = nullptr;
    }
    if (recycler) {
        recycler->notify();
    }
    destroyEngine();
    if (recycler) {
        if (buffer) {
            recycler->offer(buffer);
            buffer = nullptr;
        }
        delete recycler;
        recycler = nullptr;
    }
}

size_t AudioRecorder::read(uint8_t *buffer) {
    ObjectBox *cache = recycler->take();
    if (!cache) {
        LOGE("Cache invalid");
        return 0;
    }
    uint32_t bufSize = getBufferByteSize();
    memcpy(cache->ptr, buffer, bufSize);
    recycler->recycle(cache);
    return bufSize;
}

void AudioRecorder::AudioRecorder::flush() {

}

HwResult AudioRecorder::createEngine() {
    if (!engine) {
        ownEngine = true;
        engine = new SLEngine();
        if (!engine || !engine->valid()) {
            LOGE("AudioPlayer create failed");
            stop();
            return Hw::FAILED;
        }
    }
    return createBufferQueueObject();
}

HwResult AudioRecorder::createBufferQueueObject() {
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM,
                                   channels,
                                   sampleRate * 1000,
                                   this->format,
                                   this->format,
                                   getChannelMask(channels),
                                   SL_BYTEORDER_LITTLEENDIAN};
    // configure audio source
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE,
                                      SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT, NULL};
    SLDataSource audioSrc = {&loc_dev, NULL};

    // configure audio sink
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};

    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    // (requires the RECORD_AUDIO permission)
    const SLInterfaceID id[2] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                 SL_IID_ANDROIDCONFIGURATION};
    const SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    SLresult result = (*engine->getEngine())->CreateAudioRecorder(engine->getEngine(),
                                                                  &recordObject,
                                                                  &audioSrc, &audioSnk,
                                                                  sizeof(id) / sizeof(id[0]),
                                                                  id,
                                                                  req);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("CreateAudioRecorder failed! ret=%d", result);
        return Hw::FAILED;
    }
    // Configure the voice recognition preset which has no
    // signal processing for lower latency.
    SLAndroidConfigurationItf inputConfig;
    result = (*recordObject)->GetInterface(recordObject, SL_IID_ANDROIDCONFIGURATION,
                                           &inputConfig);
    if (SL_RESULT_SUCCESS == result) {
        SLuint32 presetValue = SL_ANDROID_RECORDING_PRESET_VOICE_RECOGNITION;
        (*inputConfig)
                ->SetConfiguration(inputConfig, SL_ANDROID_KEY_RECORDING_PRESET,
                                   &presetValue, sizeof(SLuint32));
    }
    result = (*recordObject)->Realize(recordObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder Realize failed!");
        return Hw::FAILED;
    }
    result = (*recordObject)->GetInterface(recordObject, SL_IID_RECORD, &recordItf);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder GetInterface failed!");
        return Hw::FAILED;
    }
    result = (*recordObject)->GetInterface(recordObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                           &bufferQueueItf);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder GetInterface buffer queue failed!");
        return Hw::FAILED;
    }
    result = (*bufferQueueItf)->RegisterCallback(bufferQueueItf,
                                                 bufferDequeueCallback,
                                                 this);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player RegisterCallback failed!");
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

void AudioRecorder::destroyEngine() {
    if (nullptr != recordObject) {
        (*recordObject)->Destroy(recordObject);
        recordObject = nullptr;
        bufferQueueItf = nullptr;
        recordItf = nullptr;
    }
    if (ownEngine && engine) {
        delete engine;
        engine = nullptr;
        ownEngine = false;
    }
}