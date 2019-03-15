/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/AudioPlayer.h"
#include "../include/log.h"
#include "../include/ObjectBox.h"

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
    this->lock = new SimpleLock();
    this->engine = engine;
    uint32_t bufSize = getBufferByteSize();
    this->recycler = new RecyclerBlockQueue<ObjectBox>(16, [bufSize] {
        uint8_t *buf = new uint8_t[bufSize];
        memset(buf, 0, bufSize);
        return new ObjectBox(buf);
    });
    LOGI("Create AudioPlayer, channels=%d, sampleHz=%d, minBufferSize=%d",
         this->channels,
         this->sampleRate,
         this->samplesPerBuffer);
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
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_STOPPED);
    uint32_t bufSize = getBufferByteSize();
    uint8_t *buffer = new uint8_t[bufSize];
    memset(buffer, 0, bufSize);
    write(buffer, bufSize);
    delete[]buffer;
    bufferEnqueue(bufferQueueItf);
    SLresult result = (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder SetRecordState start failed!");
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

HwResult AudioPlayer::createBufferQueueAudioPlayer() {
    SLDataLocator_AndroidSimpleBufferQueue queue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM pcm = {SL_DATAFORMAT_PCM,
                            channels,
                            sampleRate * 1000,
                            format,
                            format,
                            getChannelMask(channels),
                            SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource dataSource = {&queue, &pcm};
    SLDataLocator_OutputMix slDataLocator_outputMix = {SL_DATALOCATOR_OUTPUTMIX, mixObject};
    SLDataSink slDataSink = {&slDataLocator_outputMix, NULL};
    const SLInterfaceID ids[2] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME};
    const SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    SLresult result = (*engine->getEngine())->CreateAudioPlayer(engine->getEngine(),
                                                                &playObject,
                                                                &dataSource,
                                                                &slDataSink,
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

void AudioPlayer::bufferEnqueue(SLAndroidSimpleBufferQueueItf slBufferQueueItf) {
    LOGE("AudioPlayer...");
    auto *buffer = recycler->take();
    if (buffer) {
        (*slBufferQueueItf)->Enqueue(bufferQueueItf, buffer->ptr, getBufferByteSize());
    }
    recycler->recycle(buffer);
}

HwResult AudioPlayer::write(uint8_t *buffer, size_t size) {
    ObjectBox *cache = recycler->takeCache();
    if (!cache) {
        LOGE("Cache invalid");
        return Hw::FAILED;
    }
    memcpy(cache->ptr, buffer, size);
    recycler->offer(cache);
    return Hw::SUCCESS;
}

void AudioPlayer::flush() {
    recycler->recycleAll();
}

void AudioPlayer::stop() {
    if (recycler) {
        recycler->notify();
    }
    this->destroyEngine();
    if (recycler) {
        delete recycler;
        recycler = nullptr;
    }
    if (lock) {
        delete lock;
        lock = nullptr;
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
