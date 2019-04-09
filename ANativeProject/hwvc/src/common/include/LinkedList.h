/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_LINKEDLIST_H
#define HARDWAREVIDEOCODEC_LINKEDLIST_H

#include "List.h"
#include <iostream>

using namespace std;

template<class T>
class LinkedList : public List<T> {
    struct Item { // 链表结构
        Item(T *d, Item *nxt = 0, Item *pre = 0) : // d:保存数据，nxt:指向前一个链表结构，pre:指向下一个链表结构。
                data(d), next(nxt), prev(pre) {
        }

        T *data;
        Item *next;
        Item *prev;
    };

    int len; // 长度
    Item *head; // 链表的头指针
    Item *tail; // 链表的尾指针
    LinkedList(const LinkedList &); // 隐藏拷贝函数
public:
    LinkedList() :
            len(0), head(0), tail(0) {
    }

    virtual ~LinkedList() {
        if (len != 0) {
            while (head->next != 0) {
                Item *item = head;
                head = item->next;
                delete item;
                item = 0;
            }
        }
    }

    bool add(T *e);

    int contains(T *e);

    bool isEmpty();

    bool remove(T *e);

    int size();

    void add(int index, T *e);

    T *get(int index);

    T *remove(int index);

    void clear();

    class Iterator;

    friend class Iterator;

    class Iterator {
        LinkedList &ll;
        int index;
    public:
        Iterator(LinkedList &list) :
                ll(list), index(0) {
        }

        bool hasNext() {
            if (index < ll.len) {
                return true;
            }
            return false;
        }

        T *next() {
            if (hasNext()) {
                return ll.get(index++);
            }
            return 0;
        }
    };
};


template<class T>
bool LinkedList<T>::add(T *e) {
    add(len, e);
    return true;
}

template<class T>
int LinkedList<T>::contains(T *e) {
    Item *temp = head;
    for (int i = 0; i < len; i++) {
        if (temp->data == e) {
            return i;
        }
        temp = temp->next;
    }
    return -1;
}

template<class T>
bool LinkedList<T>::isEmpty() {
    if (len == 0) {
        return true;
    } else {
        return false;
    }
}

template<class T>
bool LinkedList<T>::remove(T *e) {
    int index = contains(e);
    if (index != -1) {
        remove(index);
        return true;
    }
    return false;
}

template<class T>
int LinkedList<T>::size() {
    return len;
}

template<class T>
void LinkedList<T>::add(int index, T *e) { // 插入
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
        len++;
    }
}

template<class T>
T *LinkedList<T>::get(int index) {
    if (index >= 0 || index < len) {
        Item *result = head;
        for (int i = 0; i < index; i++) {
            result = result->next;
        }
        return result->data;
    }
    return 0;
}

template<class T>
T *LinkedList<T>::remove(int index) {
    if (index > 0 || index <= len) {
        if (index == 0) {
            Item *temp = head;
            head = temp->next;
            head->prev = 0;
            T *result = temp->data;
            delete temp;
            temp = 0;
            --len;
            return result;
        } else if (index == len) {
            Item *temp = tail;
            tail = temp->prev;
            tail->next = 0;
            T *result = temp->data;
            delete temp;
            temp = 0;
            --len;
            return result;
        } else {
            Item *item = head;
            for (int i = 0; i < index; i++) {
                item = item->next;
            }
            Item *itemPrev = item->prev;
            Item *itemNext = item->next;
            itemPrev->next = itemNext;
            itemNext->prev = itemPrev;
            T *result = item->data;
            delete item;
            item = 0;
            --len;
            return result;
        }
    }
    return 0;
}

template<class T>
void LinkedList<T>::clear() {
    Item *item = head;
    for (int i = 0; i < len; i++) {
        Item *tmp = item;
        item = item->next;
        delete temp;
    }
    head = nullptr;
    tail = nullptr;
}

#endif //HARDWAREVIDEOCODEC_LINKEDLIST_H
