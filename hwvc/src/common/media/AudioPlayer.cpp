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

AudioPlayer::AudioPlayer(int channels, int sampleHz, int format, int minBufferSize) {
    initialize(nullptr, channels, sampleHz, format, minBufferSize);
}

AudioPlayer::AudioPlayer(SLEngine *engine, int channels, int sampleHz, int format,
                         int minBufferSize) {
    initialize(engine, channels, sampleHz, format, minBufferSize);
}

void AudioPlayer::initialize(SLEngine *engine, int channels, int sampleHz, int format,
                             int minBufferSize) {
    this->lock = new SimpleLock();
    this->engine = engine;
    this->channels = channels;
    this->sampleHz = sampleHz;
    this->format = format;
    this->minBufferSize = minBufferSize;
    this->recycler = new RecyclerBlockQueue<ObjectBox>(16, [minBufferSize] {
        uint8_t *buf = new uint8_t[minBufferSize];
        memset(buf, 0, minBufferSize);
        return new ObjectBox(buf);
    });
    LOGI("Create AudioPlayer, channels=%d, sampleHz=%d, minBufferSize=%d",
         this->channels,
         this->sampleHz,
         this->minBufferSize);
    mixObject = nullptr;
    playObject = nullptr;
    playItf = nullptr;
    int ret = this->createEngine();
    if (!ret) {
        LOGE("AudioPlayer create failed");
    }

}

AudioPlayer::~AudioPlayer() {
    LOGI("~AudioPlayer");
    stop();
}

int AudioPlayer::createEngine() {
    if (!engine) {
        ownEngine = true;
        engine = new SLEngine();
        if (!engine || !engine->valid()) {
            LOGE("AudioPlayer create failed");
            stop();
            return 0;
        }
    }

    SLresult result = (*engine->getEngine())->CreateOutputMix(engine->getEngine(), &mixObject, 0,
                                                              nullptr, nullptr);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("CreateOutputMix failed!");
        return 0;
    }
    result = (*mixObject)->Realize(mixObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("OutputMix Realize failed!");
        return 0;
    }
    return createBufferQueueAudioPlayer();
}

int AudioPlayer::start() {
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_STOPPED);
    uint8_t *buffer = new uint8_t[minBufferSize];
    memset(buffer, 0, minBufferSize);
    write(buffer, minBufferSize);
    delete[]buffer;
    bufferEnqueue(bufferQueueItf);
    SLresult result = (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder SetRecordState start failed!");
        return 0;
    }
    return 1;
}

int AudioPlayer::createBufferQueueAudioPlayer() {
    SLDataLocator_AndroidSimpleBufferQueue queue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM pcm = {SL_DATAFORMAT_PCM,
                            channels,
                            sampleHz * 1000,
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
        return 0;
    }
    result = (*playObject)->Realize(playObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player Realize failed!");
        return 0;
    }
    result = (*playObject)->GetInterface(playObject, SL_IID_PLAY, &playItf);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player GetInterface failed!");
        return 0;
    }
    result = (*playObject)->GetInterface(playObject, SL_IID_BUFFERQUEUE, &bufferQueueItf);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player GetInterface buffer queue failed!");
        return 0;
    }
    result = (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_STOPPED);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player SetPlayState stop failed!");
        return 0;
    }
    result = (*bufferQueueItf)->RegisterCallback(bufferQueueItf,
                                                 bufferQueueCallback,
                                                 this);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player RegisterCallback failed!");
        return 0;
    }
    return 1;
}

void AudioPlayer::bufferEnqueue(SLAndroidSimpleBufferQueueItf slBufferQueueItf) {
    LOGE("AudioPlayer...");
    auto *buffer = recycler->take();
    if (buffer) {
        (*slBufferQueueItf)->Enqueue(bufferQueueItf, buffer->ptr, minBufferSize);
    }
    recycler->recycle(buffer);
}

int AudioPlayer::write(uint8_t *buffer, size_t size) {
    ObjectBox *cache = recycler->takeCache();
    if (!cache) {
        LOGE("Cache invalid");
        return 0;
    }
    memcpy(cache->ptr, buffer, size);
    recycler->offer(cache);
    return 1;
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
