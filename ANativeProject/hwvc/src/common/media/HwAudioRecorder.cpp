/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/HwAudioRecorder.h"

void bufferDequeueCallback(SLAndroidSimpleBufferQueueItf slBufferQueueItf, void *context) {
    HwAudioRecorder *recorder = static_cast<HwAudioRecorder *>(context);
    recorder->bufferDequeue(slBufferQueueItf);
}

void HwAudioRecorder::bufferDequeue(SLAndroidSimpleBufferQueueItf slBufferQueueItf) {
    if (this->buffer) {
        Logcat::i("HWVC", "HwAudioRecorder...");
        (*slBufferQueueItf)->Enqueue(slBufferQueueItf, buffer->getData(), buffer->size());
        fwrite(buffer->getData(), 1, buffer->size(), pcmFile);
        if (fifo) {
            fifo->push(buffer->getData(), buffer->size());
        }
    }
}

HwAudioRecorder::HwAudioRecorder(uint16_t channels,
                                 uint32_t sampleRate,
                                 uint16_t format,
                                 uint32_t samplesPerBuffer) : SLAudioDevice(channels,
                                                                            sampleRate,
                                                                            format,
                                                                            samplesPerBuffer) {
    initialize(nullptr);
}

HwAudioRecorder::HwAudioRecorder(SLEngine *engine,
                                 uint16_t channels,
                                 uint32_t sampleRate,
                                 uint16_t format,
                                 uint32_t samplesPerBuffer) : SLAudioDevice(channels,
                                                                            sampleRate,
                                                                            format,
                                                                            samplesPerBuffer) {
    initialize(engine);
}

void HwAudioRecorder::initialize(SLEngine *engine) {
    pcmFile = fopen("/sdcard/pcm_sl.pcm", "wb");
    this->engine = engine;
    LOGI("Create HwAudioRecorder, channels=%d, sampleHz=%d",
         this->channels,
         this->sampleRate);
    uint32_t bufSize = getBufferByteSize() * 16;
    this->fifo = new HwFIFOBuffer(bufSize, false);
    this->buffer = HwBuffer::alloc(getBufferByteSize());
    HwResult ret = this->createEngine();
    if (Hw::SUCCESS != ret) {
        LOGE("HwAudioRecorder start failed");
    }

}

HwAudioRecorder::~HwAudioRecorder() {
    LOGI("HwAudioRecorderer");
    stop();
}

HwResult HwAudioRecorder::start() {
    HwResult ret = recorder->stop();
    if (Hw::SUCCESS != ret) {
        return ret;
    }
    ret = recorder->clear();
    if (Hw::SUCCESS != ret) {
        return ret;
    }
    bufferDequeue(recorder->getQueue());
    ret = recorder->start();
    if (Hw::SUCCESS != ret) {
        LOGE("Recorder SetRecordState start failed!");
        return ret;
    }
    return Hw::SUCCESS;
}

void HwAudioRecorder::stop() {
    if (recorder) {
        HwResult ret = recorder->stop();
        if (Hw::SUCCESS != ret) {
            LOGE("Recorder SetRecordState stop failed!");
        }
    }
    if (pcmFile) {
        fclose(pcmFile);
        pcmFile = nullptr;
    }
    if (buffer) {
        delete buffer;
        buffer = nullptr;
    }
    if (fifo) {
        delete fifo;
        fifo = nullptr;
    }
}

HwBuffer *HwAudioRecorder::read(size_t size) {
    if (!fifo) {
        Logcat::e("HWVC", "FIFO invalid");
        return 0;
    }
    return fifo->take(size);
}

void HwAudioRecorder::flush() {

}

HwResult HwAudioRecorder::createEngine() {
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

HwResult HwAudioRecorder::createBufferQueueObject() {
    recorder = new SLRecorder(engine);
    HwResult ret = recorder->initialize(this);
    if (Hw::FAILED == ret) {
        return ret;
    }
    ret = recorder->registerCallback(bufferDequeueCallback, this);
    if (Hw::FAILED == ret) {
        return ret;
    }
    return Hw::SUCCESS;
}

void HwAudioRecorder::destroyEngine() {
    if (recorder) {
        delete recorder;
        recorder = nullptr;
    }
    if (ownEngine && engine) {
        delete engine;
        engine = nullptr;
        ownEngine = false;
    }
}