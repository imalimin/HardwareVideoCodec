/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "Thread.h"
#include "../entity/Object.h"
#include "MessageQueue.h"

class HandlerThread : public Object {
private:
    Thread *thread = nullptr;
    MessageQueue queue = nullptr;
};


#endif //HARDWAREVIDEOCODEC_HANDLERTHREAD_H
