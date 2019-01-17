/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_LINKEDSTACK_H
#define HARDWAREVIDEOCODEC_LINKEDSTACK_H

#include "Collection.h"
#include "../include/log.h"

using namespace std;

template<class T>
class LinkedQueue : public Collection<T> {
    class Item {
    public:
        Item(T *d, Item *nxt = 0, Item *pre = 0) :
                data(d), next(nxt), prev(pre) {
        }

        ~Item() {
            next = nullptr;
            prev = nullptr;
            if (data) {
                delete data;
            }
        }

        T *data;
        Item *next;
        Item *prev;
    };

    int len;
    Item *head;
    Item *tail;

    LinkedQueue(const LinkedQueue &);

public:

    LinkedQueue() : len(0), head(0), tail(0) {
    }

    ~LinkedQueue() {
        clear();
    }

    void offer(T *e);

    T *take();

    bool empty();

    void clear();

    bool add(T *e) override;

    int contains(T *e) override;

    bool isEmpty() override;

    bool remove(T *e) override;

    int size() override;
};

template<class T>
void LinkedQueue<T>::offer(T *e) {
    int index = len;
    if (index > 0 || index <= len) {
        if (len == 0) {
            Item *first = new Item(e);
            head = first;
            tail = first;
        } else if (index == 0) {
            Item *temp = new Item(e, head, 0);
            head->prev = temp;
            head = temp;
        } else if (index == len) {
            Item *temp = new Item(e, 0, tail);
            tail->next = temp;
            tail = temp;
        } else {
            Item *itemPrev = head;
            for (int i = 1; i < index; i++) {
                itemPrev = itemPrev->next;
            }
            Item *itemNext = itemPrev->next;
            Item *newItem = new Item(e, itemNext, itemPrev);
            itemPrev->next = newItem;
            itemNext->prev = newItem;
        }
        ++len;
    }
}

template<class T>
T *LinkedQueue<T>::take() {
    if (0 == len) {
        return nullptr;
    }
    Item *result = head;
    head = head->next;
    --len;
    return result->data;
}

template<class T>
int LinkedQueue<T>::size() {
    return len;
}

template<class T>
bool LinkedQueue<T>::empty() {
    return isEmpty();
}

template<class T>
void LinkedQueue<T>::clear() {
    if (len != 0) {
        while (head->next != nullptr) {
            Item *item = head;
            head = item->next;
            delete item;
            item = nullptr;
        }
    }
}

template<class T>
bool LinkedQueue<T>::add(T *e) {
    return false;
}

template<class T>
int LinkedQueue<T>::contains(T *e) {
    return 0;
}

template<class T>
bool LinkedQueue<T>::isEmpty() {
    return 0 == len;
}

template<class T>
bool LinkedQueue<T>::remove(T *e) {
    return nullptr;
}


#endif //HARDWAREVIDEOCODEC_LINKEDSTACK_H
