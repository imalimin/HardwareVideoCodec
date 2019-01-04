/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_LINKEDSTACK_H
#define HARDWAREVIDEOCODEC_LINKEDSTACK_H

#include "Object.h"
#include "../include/log.h"

template<class T>
class Node : public Object {
public:
    T *data;
    Node *next;

    Node(T *data) {
        this->data = data;
        next = nullptr;
    }

    ~Node() {
        next = nullptr;
    }
};

template<class T>
class LinkedStack : public Object {
public:
    typedef Node<T> NodeT;

    LinkedStack() {
        head = nullptr;
        len = 0;
    }

    ~LinkedStack() {
        if (head) {
            clear();
        }
    }

    void offer(T *e) {
        if (!head) {
            head = new NodeT(e);
            head->next = nullptr;
        } else {
            NodeT *node = new NodeT(e);
            node->next = head;
            head = node;
        }
        ++len;
    }

    T *take() {
        if (0 == len)
            return nullptr;
        if (1 == len) {
            T *e = head->data;
            delete head;
            head = nullptr;
            --len;
            return e;
        }
        NodeT *tmp = head;
        for (int i = 0; i < len - 2; ++i) {
            tmp = tmp->next;
        }
        NodeT *node = tmp->next;
        tmp->next = nullptr;
        T *e = node->data;
        node->data = nullptr;
        delete node;
        --len;
        return e;
    }

    int size() {
        return len;
    }

    bool empty() {
        return 0 == len;
    }

    void clear() {
        NodeT *tmp;
        for (int i = 0; i < len; i++) {
            tmp = head;
            head = head->next;
            delete tmp;
        }
        head = nullptr;
        len = 0;
    }

private:
    int len = 0;
    NodeT *head = nullptr;
};


#endif //HARDWAREVIDEOCODEC_LINKEDSTACK_H
