/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/AudioPlayer.h"
#include "../include/log.h"
#include "../include/ObjectBox.h"
#include "../include/TimeUtils.h"

void bufferQueueCallback(SLAndroidSimpleBufferQueueItf slBufferQueueItf, void *context) {
    AudioPlayer *player = static_cast<AudioPlayer *>(context);
    player->bufferEnqueue(slBufferQueueItf);
}

AudioPlayer::AudioPlayer(uint16_t channels,
                         uint32_t sampleRate,
                         uint16_t format,
                         uint32_t samplesPerBuffer) : SLAudioDevice(channels,
                                                                    sampleRate,
                                                                    format,
                                                                    samplesPerBuffer) {
    initialize(nullptr);
}

AudioPlayer::AudioPlayer(SLEngine *engine,
                         uint16_t channels,
                         uint32_t sampleRate,
                         uint16_t format,
                         uint32_t samplesPerBuffer) : SLAudioDevice(channels,
                                                                    sampleRate,
                                                                    format,
                                                                    samplesPerBuffer) {
    initialize(engine);
}

void AudioPlayer::initialize(SLEngine *engine) {
    this->engine = engine;
    uint32_t bufSize = sampleRate * channels * format * 0.5;
    bufSize = (bufSize + 7) >> 3;
    this->fifo = new HwFIFOBuffer(bufSize);
    LOGI("Create AudioPlayer, channels=%d, sampleHz=%d, minBufferSize=%d, format=%d",
         this->channels,
         this->sampleRate,
         this->samplesPerBuffer,
         this->format);
    mixObject = nullptr;
    playObject = nullptr;
    playItf = nullptr;
    HwResult ret = this->createEngine();
    if (Hw::SUCCESS != ret) {
        LOGE("AudioPlayer create failed");
    }

}

AudioPlayer::~AudioPlayer() {
    LOGI("~AudioPlayer");
    stop();
}

HwResult AudioPlayer::createEngine() {
    if (!engine) {
        ownEngine = true;
        engine = new SLEngine();
        if (!engine || !engine->valid()) {
            LOGE("AudioPlayer create failed");
            stop();
            return Hw::FAILED;
        }
    }

    SLresult result = (*engine->getEngine())->CreateOutputMix(engine->getEngine(), &mixObject, 0,
                                                              nullptr, nullptr);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("CreateOutputMix failed!");
        return Hw::FAILED;
    }
    result = (*mixObject)->Realize(mixObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("OutputMix Realize failed!");
        return Hw::FAILED;
    }
    return createBufferQueueAudioPlayer();
}

HwResult AudioPlayer::start() {
    file = fopen("/sdcard/3.pcm", "wb");
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_STOPPED);
    uint32_t bufSize = getBufferByteSize();
    uint8_t buffer[bufSize];
    memset(buffer, 0, bufSize);
    write(buffer, bufSize);
    bufferEnqueue(bufferQueueItf);
    SLresult result = (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder SetRecordState start failed!");
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

HwResult AudioPlayer::createBufferQueueAudioPlayer() {
    // configure audio source
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};

    SLAndroidDataFormat_PCM_EX format_pcm = {SL_DATAFORMAT_PCM,
                                             channels,
                                             sampleRate * 1000,
                                             format,
                                             format,
                                             getChannelMask(),
                                             SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX,
                                          mixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};
    /*
     * create fast path audio player: SL_IID_BUFFERQUEUE and SL_IID_VOLUME
     * and other non-signal processing interfaces are ok.
     */
    SLInterfaceID ids[2] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME};
    SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    SLresult result = (*engine->getEngine())->CreateAudioPlayer(engine->getEngine(),
                                                                &playObject,
                                                                &audioSrc,
                                                                &audioSnk,
                                                                sizeof(ids) / sizeof(ids[0]),
                                                                ids,
                                                                req);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("CreateAudioPlayer failed! ret=%d", result);
        return Hw::FAILED;
    }
    result = (*playObject)->Realize(playObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player Realize failed!");
        return Hw::FAILED;
    }
    result = (*playObject)->GetInterface(playObject, SL_IID_PLAY, &playItf);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player GetInterface failed!");
        return Hw::FAILED;
    }
    result = (*playObject)->GetInterface(playObject, SL_IID_BUFFERQUEUE, &bufferQueueItf);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player GetInterface buffer queue failed!");
        return Hw::FAILED;
    }
    result = (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_STOPPED);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player SetPlayState stop failed!");
        return Hw::FAILED;
    }
    result = (*bufferQueueItf)->RegisterCallback(bufferQueueItf,
                                                 bufferQueueCallback,
                                                 this);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player RegisterCallback failed!");
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

static int64_t ttime = 0;

void AudioPlayer::bufferEnqueue(SLAndroidSimpleBufferQueueItf slBufferQueueItf) {
//    if (!recycler) {
//        return;
//    }
//    auto *buffer = recycler->take();
//    if (buffer) {
//        (*slBufferQueueItf)->Enqueue(bufferQueueItf, buffer->ptr, getBufferByteSize());
//    }
//    recycler->recycle(buffer);
    //-----------------------
//    ObjectBox *buffer = nullptr;
//    if (pcmList.size() > 0) {
//        buffer = pcmList.front();
//        pcmList.pop();
//    } else {
//        buffer = new ObjectBox(new uint8_t[getBufferByteSize()]);
//        memset(buffer->ptr, 0, getBufferByteSize());
//    }
//    (*slBufferQueueItf)->Enqueue(bufferQueueItf, buffer->ptr, getBufferByteSize());
//    delete buffer;
    //-----------------------
    if (!fifo) {
        return;
    }
    Logcat::i("HWVC", "AudioPlayer..., %d, %lld", fifo->size(), getCurrentTimeUS() - ttime);
    ttime = getCurrentTimeUS();
    HwAbsFrame *frame = fifo->take(getBufferByteSize());
    if (frame) {
        (*slBufferQueueItf)->Enqueue(bufferQueueItf, frame->getData(), frame->getDataSize());
        if (file) {
            fwrite(frame->getData(), 1, frame->getDataSize(), file);
        }
        delete frame;
        return;
    }
    uint8_t *buffer = new uint8_t[getBufferByteSize()];
    memset(buffer, 0, getBufferByteSize());
    (*slBufferQueueItf)->Enqueue(bufferQueueItf, buffer, getBufferByteSize());
}

HwResult AudioPlayer::write(uint8_t *buffer, size_t size) {
//    ObjectBox *cache = recycler->takeCache();
//    if (!cache) {
//        LOGE("Cache invalid");
//        return Hw::FAILED;
//    }
//    memcpy(cache->ptr, buffer, size);
//    recycler->offer(cache);
    //------------------
//    uint8_t *data = new uint8_t[size];
//    memcpy(data, buffer, size);
//    pcmList.push(new ObjectBox(data));
    //-----------------
    size_t ret = fifo->push(buffer, size);
    if (0 == ret) {
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

void AudioPlayer::flush() {
    if (fifo) {
        fifo->flush();
    }
}

void AudioPlayer::stop() {
    Logcat::i("HWVC", "AudioPlayer::stop");
    this->destroyEngine();
    if (fifo) {
        delete fifo;
        fifo = nullptr;
    }
    if (file) {
        fclose(file);
        file = nullptr;
    }
}

void AudioPlayer::destroyEngine() {
    if (nullptr != playObject) {
        (*playObject)->Destroy(playObject);
        playObject = nullptr;
        bufferQueueItf = nullptr;
        playItf = nullptr;
    }
    if (nullptr != mixObject) {
        (*mixObject)->Destroy(mixObject);
        mixObject = nullptr;
    }
    if (ownEngine && engine) {
        delete engine;
        engine = nullptr;
        ownEngine = false;
    }
}
