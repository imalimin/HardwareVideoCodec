/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_LINKEDLIST_H
#define HARDWAREVIDEOCODEC_LINKEDLIST_H

#include <memory>
#include <stdexcept>
#include <vector>
#include <algorithm>

using std::shared_ptr;
using std::make_shared;

template<typename Data>
class List {
    friend class const_iterator;

private:
    struct Node {
        Node() = default;

        Data data;
        shared_ptr <Node> prev = nullptr;
        shared_ptr <Node> next = nullptr;
    };

    shared_ptr <Node> head = make_shared<Node>();
    shared_ptr <Node> tail = make_shared<Node>();

    void initialize() {
        head->next = tail;
        tail->prev = head;
    }

    size_t List_Size = 0;

public:
    // CLASS const_iterator
    class const_iterator {
        friend class List<Data>;

    public:
        const_iterator() = default;

        ~const_iterator() = default;

        const_iterator(shared_ptr <Node> p) : ptr(p) {}

        const Data &operator*() const {
            return ptr->data;
        }

        const_iterator &operator+(long long LL) {
            for (auto i = 0; i != LL; ++i) {
                if (ptr->next != nullptr) {
                    ptr = ptr->next;
                }
            }
            return *this;
        }

        const_iterator &operator-(long long LL) {
            for (auto i = 0; i != LL; ++i) {
                if (ptr->prev != nullptr) {
                    ptr = ptr->prev;
                }
            }
            return *this;
        }

        const_iterator &operator++() {
            ptr = ptr->next;
            return *this;
        }

        const_iterator &operator++(int) {
            const_iterator temp = *this;
            ++*this;
            return temp;
        }

        const_iterator &operator--() {
            ptr = ptr->prev;
            return *this;
        }

        const_iterator &operator--(int) {
            const_iterator temp = *this;
            --*this;
            return temp;
        }

        bool operator==(const const_iterator &rhs) const {
            return ptr == rhs.ptr;
        }

        bool operator!=(const const_iterator &rhs) const {
            return !(*this == rhs);
        }

    protected:
        shared_ptr <Node> ptr;
    };

    // CLASS iterator
    class iterator : public const_iterator {
        friend class List<Data>;

    public:
        iterator() = default;

        ~iterator() = default;

        Data &operator*() {
            return ptr->data;
        }

        const Data &operator*() const {
            return const_iterator::operator*();
        }

        iterator &operator+(long long LL) {
            for (auto i = 0; i != LL; ++i) {
                if (ptr->next != nullptr) {
                    ptr = ptr->next;
                }
            }
            return *this;
        }

        iterator &operator-(long long LL) {
            for (auto i = 0; i != LL; ++i) {
                if (ptr->prev != nullptr) {
                    ptr = ptr->prev;
                }
            }
            return *this;
        }

        iterator &operator++() {
            ptr = ptr->next;
            return *this;
        }

        iterator &operator++(int) {
            iterator temp = *this;
            ++*this;
            return temp;
        }

        iterator &operator--() {
            ptr = ptr->prev;
            return *this;
        }

        iterator &operator--(int) {
            iterator temp = *this;
            --*this;
            return temp;
        }

    protected:
        iterator(shared_ptr <Node> p) : const_iterator(p) {}
    };

    // CLASS const_reverse_iterator
    class const_reverse_iterator {
        friend class List<Data>;

    public:
        const_reverse_iterator() = default;

        ~const_reverse_iterator() = default;

        const_reverse_iterator(shared_ptr <Node> p) : ptr(p) {}

        const Data &operator*() const {
            return ptr->data;
        }

        const_reverse_iterator &operator+(long long LL) {
            for (auto i = 0; i != LL; ++i) {
                if (ptr->prev != nullptr) {
                    ptr = ptr->prev;
                }
            }
            return *this;
        }

        const_reverse_iterator &operator-(long long LL) {
            for (auto i = 0; i != LL; ++i) {
                if (ptr->next != nullptr) {
                    ptr = ptr->next;
                }
            }
            return *this;
        }

        const_reverse_iterator &operator++() {
            ptr = ptr->prev;
            return *this;
        }

        const_reverse_iterator &operator++(int) {
            const_reverse_iterator temp = *this;
            ++*this;
            return temp;
        }

        const_reverse_iterator &operator--() {
            ptr = ptr->next;
            return *this;
        }

        const_reverse_iterator &operator--(int) {
            const_reverse_iterator temp = *this;
            --*this;
            return temp;
        }

        bool operator==(const const_reverse_iterator &rhs) {
            return ptr == rhs.ptr;
        }

        bool operator!=(const const_reverse_iterator &rhs) {
            return !(*this == rhs);
        }

    protected:
        shared_ptr <Node> ptr;
    };

    // CLASS reverse_iterator
    class reverse_iterator : public const_reverse_iterator {
        friend class List<Data>;

    public:
        reverse_iterator() = default;

        ~reverse_iterator() = default;

        Data &operator*() {
            return ptr->data;
        }

        const Data &operator*() const {
            return const_reverse_iterator::operator*();
        }

        reverse_iterator &operator+(long long LL) {
            for (auto i = 0; i != LL; ++i) {
                if (ptr->prev != nullptr) {
                    ptr = ptr->prev;
                }
            }
            return *this;
        }

        reverse_iterator &operator-(long long LL) {
            for (auto i = 0; i != LL; ++i) {
                if (ptr->next != nullptr) {
                    ptr = ptr->next;
                }
            }
            return *this;
        }

        reverse_iterator &operator++() {
            ptr = ptr->prev;
            return *this;
        }

        reverse_iterator &operator++(int) {
            reverse_iterator temp = *this;
            ++*this;
            return temp;
        }

        reverse_iterator &operator--() {
            ptr = ptr->next;
            return *this;
        }

        reverse_iterator &operator--(int) {
            reverse_iterator temp = *this;
            --*this;
            return temp;
        }

    protected:
        reverse_iterator(shared_ptr <Node> p) : const_reverse_iterator(p) {}
    };

public:
    List() { initialize(); }

    ~List() = default;

    //List(const List &rhs) {
    //  clear();
    //  for (auto &i : rhs) {
    //      push_back(i);
    //  }
    //}
    //List& operator=(const List &rhs) {
    //  if (this == &rhs) {
    //      return *this;
    //  }
    //  clear();
    //  for (auto &i : rhs) {
    //      push_back(i);
    //  }
    //  return *this;
    //}

    iterator begin() {
        return iterator(head->next);
    }

    const_iterator begin() const {
        return const_iterator(head->next);
    }

    reverse_iterator rbegin() {
        return reverse_iterator(tail->prev);
    }

    const_reverse_iterator rbegin() const {
        return const_reverse_iterator(tail->prev);
    }

    iterator end() {
        return iterator(tail);
    }

    const_iterator end() const {
        return const_iterator(tail);
    }

    reverse_iterator rend() {
        return reverse_iterator(head);
    }

    const_reverse_iterator rend() const {
        return const_reverse_iterator(head);
    }

    // Front insert.
    iterator front_insert(iterator itr, const Data &data) {
        shared_ptr <Node> p = itr.ptr;
        ++List_Size;
        auto a = make_shared<Node>();
        a->data = data;
        a->prev = p->prev;
        a->next = p;
        p->prev = a;
        a->prev->next = a;
        return iterator(a);
        //return iterator(p->prev = p->prev->next = make_shared<Node>(data, p->prev, p));
    }

    iterator front_insert(iterator itr) {
        shared_ptr <Node> p = itr.ptr;
        ++List_Size;
        auto a = make_shared<Node>();
        a->prev = p->prev;
        a->next = p;
        p->prev = a;
        a->prev->next = a;
        return iterator(a);
    }

    // Back_insert.
    iterator back_insert(iterator itr, const Data &data) {
        shared_ptr <Node> p = itr.ptr;
        ++List_Size;
        auto a = make_shared<Node>();
        a->data = data;
        a->prev = p;
        a->next = p->next;
        p->next = a;
        a->next->prev = a;
        return iterator(a);
        //return iterator(p->next = p->next->prev = make_shared<Node>(data, p, p->next));
    }

    iterator back_insert(iterator itr) {
        shared_ptr <Node> p = itr.ptr;
        ++List_Size;
        auto a = make_shared<Node>();
        a->prev = p;
        a->next = p->next;
        p->next = a;
        a->next->prev = a;
        return iterator(a);
    }

    iterator erase(iterator itr) {
        shared_ptr <Node> p = itr.ptr;
        iterator itaval(p->next);
        try {
            if (p == head) {
                throw runtime_error("No elements can erase!");
            } else if (p == tail) {
                throw runtime_error("No elements can erase!");
            } else {
                p->prev->next = p->next;
                p->next->prev = p->prev;
                --List_Size;
            }
        }
        catch (runtime_error err) {
            cout << err.what() << endl;
        }
        return itaval;
    }

    iterator erase(iterator begin, iterator end) {  // [begin, end)
        while (begin != end) {
            begin = erase(begin);
        }
        return iterator(end);
    }

    void clear() {
        while (!empty()) {
            pop_front();
        }
    }

    void print() {
        for (auto &i : *this) {
            cout << i << endl;
        }
    }

    void resize(long long LL) {
        for (auto i = 0; i != LL; ++i) {
            push_back();
        }
    }

    void resize(long long LL, const Data &x) {
        for (auto i = 0; i != LL; ++i) {
            push_back(x);
        }
    }

    void sort() {
        std::vector <Data> temp_v;
        for (auto &i : *this) {
            temp_v.push_back(i);
        }
        std::stable_sort(temp_v.begin(), temp_v.end());
        clear();
        for (auto &i : temp_v) {
            push_back(i);
        }
    }

    iterator search(const Data &x) {
        auto temp_count = 0;
        for (auto &i : *this) {
            if (i != x) {
                ++temp_count;
            } else {
                break;
            }
        }
        return begin() + temp_count;
    }

    bool empty() { return List_Size == 0; }

    size_t size() { return List_Size; }

    Data &front() { return *begin(); }

    const Data &front() const { return front(); }

    Data &back() { return *--end(); }

    const Data &back() const { return back(); }

    void push_front() { front_insert(begin()); }

    void push_front(const Data &x) { front_insert(begin(), x); }

    void push_back() { back_insert(--end()); }

    void push_back(const Data &x) { back_insert(--end(), x); }

    void pop_front() { erase(begin()); }

    void pop_back() { erase(--end()); }

};

#endif //HARDWAREVIDEOCODEC_LINKEDLIST_H
