/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../../include/SLRecorder.h"
#include "../../include/log.h"

SLRecorder::SLRecorder(SLEngine *engine) {
    this->engine = engine;
}

SLRecorder::~SLRecorder() {
    release();
}

HwResult SLRecorder::initialize(SLAudioDevice *device) {
    SLAndroidDataFormat_PCM_EX format_pcm = {SL_DATAFORMAT_PCM,
                                   device->getChannels(),
                                   device->getSampleRate() * 1000,
                                   device->getFormat(),
                                   device->getFormat(),
                                   device->getChannelMask(),
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
//        SLuint32 mode = SL_ANDROID_PERFORMANCE_NONE;
//        (*inputConfig)
//                ->SetConfiguration(inputConfig, SL_ANDROID_KEY_PERFORMANCE_MODE,
//                                   &presetValue, sizeof(SLuint32));
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
    return Hw::SUCCESS;
}

HwResult SLRecorder::registerCallback(slAndroidSimpleBufferQueueCallback callback,
                                      void *pContext) {
    if (!bufferQueueItf) {
        LOGE("Please call initialize method before!");
        return Hw::FAILED;
    }
    SLresult result = (*bufferQueueItf)->RegisterCallback(bufferQueueItf,
                                                          callback,
                                                          pContext);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder RegisterCallback failed!");
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

HwResult SLRecorder::start() {
    if (!recordItf) {
        LOGE("Please call initialize method before!");
        return Hw::FAILED;
    }
    SLresult result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder start failed!");
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

HwResult SLRecorder::stop() {
    if (!recordItf) {
        LOGE("Please call initialize method before!");
        return Hw::FAILED;
    }
    SLresult result = (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder stop failed!");
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

HwResult SLRecorder::clear() {
    if (!bufferQueueItf) {
        LOGE("Please call initialize method before!");
        return Hw::FAILED;
    }
    SLresult result = (*bufferQueueItf)->Clear(bufferQueueItf);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("Recorder clear failed!");
        return Hw::FAILED;
    }
    return Hw::SUCCESS;
}

SLAndroidSimpleBufferQueueItf SLRecorder::getQueue() {
    return bufferQueueItf;
}

void SLRecorder::release() {
    if (recordObject) {
        (*recordObject)->Destroy(recordObject);
        recordObject = nullptr;
        bufferQueueItf = nullptr;
        recordItf = nullptr;
    }
    engine = nullptr;
}