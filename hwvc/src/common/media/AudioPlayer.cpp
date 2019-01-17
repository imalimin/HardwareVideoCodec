/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/AudioPlayer.h"
#include "../include/log.h"
#include "../include/ObjectBox.h"

void bufferQueueCallback(SLBufferQueueItf slBufferQueueItf, void *context) {
    AudioPlayer *player = static_cast<AudioPlayer *>(context);
    player->bufferEnqueue(slBufferQueueItf);
}

AudioPlayer::AudioPlayer(int channels, int sampleHz, int format, int minBufferSize) {
    this->lock = new SimpleLock();
    this->pipeline = new EventPipeline("AudioPlayer");
    this->channels = channels;
    this->sampleHz = sampleHz;
    this->format = format;
    this->minBufferSize = minBufferSize;
    this->recycler = new RecyclerBlockQueue<ObjectBox>(16, [minBufferSize] {
        return new ObjectBox(new uint8_t[minBufferSize]);
    });
    LOGI("Create AudioPlayer, channels=%d, sampleHz=%d, minBufferSize=%d",
         this->channels,
         this->sampleHz,
         this->minBufferSize);
    engineObject = nullptr;
    engineItf = nullptr;
    mixObject = nullptr;
    playObject = nullptr;
    playItf = nullptr;
}

AudioPlayer::~AudioPlayer() {
    LOGI("~AudioPlayer");
    stop();
}

int AudioPlayer::createEngine() {
    SLresult result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("slCreateEngine failed!");
        return 0;
    }
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Engine Realize failed!");
        return 0;
    }
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineItf);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Engine GetInterface failed!");
        return 0;
    }

    result = (*engineItf)->CreateOutputMix(engineItf, &mixObject, 0, nullptr, nullptr);
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
    if (!pipeline) {
        return 0;
    }
    pipeline->queueEvent([this] {
        int ret = this->createEngine();
        if (!ret) {
            LOGE("AudioPlayer start failed");
            this->stop();
        }
    });
    return 1;
}

string AudioPlayer::getString() {
    return "Test";
}

static SLuint32 getChannelMask(int channels) {
    switch (channels) {
        case 1:
            return SL_SPEAKER_FRONT_CENTER;
        case 3:
            return SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT | SL_SPEAKER_FRONT_CENTER;
        case 2:
        default:
            return SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
    }
}

int AudioPlayer::createBufferQueueAudioPlayer() {
    SLDataLocator_BufferQueue queue = {SL_DATALOCATOR_BUFFERQUEUE, 2};
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
    SLresult result = (*engineItf)->CreateAudioPlayer(engineItf,
                                                      &playObject,
                                                      &dataSource,
                                                      &slDataSink,
                                                      2,
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
    result = (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Player SetPlayState start failed!");
        return 0;
    }
    buffer = new ObjectBox(new uint8_t[minBufferSize]);
    (*bufferQueueItf)->Enqueue(bufferQueueItf, buffer->ptr, minBufferSize);
    return 1;
}

void AudioPlayer::bufferEnqueue(SLBufferQueueItf slBufferQueueItf) {
    if (buffer) {
        recycler->recycle(buffer);
    }
    buffer = recycler->take();
    if (buffer) {
        (*slBufferQueueItf)->Enqueue(bufferQueueItf, buffer->ptr, minBufferSize);
    }
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
    pipeline->queueEvent([this] {
        recycler->recycleAll();
    });
}

void AudioPlayer::stop() {
    if (recycler) {
        recycler->notify();
    }
    if (pipeline) {
        pipeline->queueEvent([this] {
            this->destroyEngine();
        });
        delete pipeline;
        pipeline = nullptr;
    }
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
    if (nullptr != engineObject) {
        (*engineObject)->Destroy(engineObject);
        engineObject = nullptr;
        engineItf = nullptr;
    }
}
