/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import com.lmy.codec.BuildConfig
import com.lmy.codec.texture.impl.BaseTexture
import com.lmy.codec.texture.impl.sticker.BaseSticker
import com.lmy.codec.util.debug_i
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by lmyooyo@gmail.com on 2018/9/30.
 */
class GroupFilter private constructor(filter: BaseFilter,
                                      private var deFlashing: Boolean,
                                      width: Int = 0,
                                      height: Int = 0,
                                      textureId: IntArray = IntArray(1))
    : BaseFilter(width, height, textureId) {
    private val initList = LinkedBlockingQueue<BaseTexture>()
    private val releaseList = LinkedBlockingQueue<BaseTexture>()
    private val filters = ArrayList<BaseFilter>()
    private val stickers = ArrayList<BaseSticker>()
    //For resolve flash
    private var lastFilter: NormalFilter? = null

    init {
        addFilter(filter)
    }

    fun addFilter(filter: BaseFilter): GroupFilter {
        addToInitList(filter)
        return this
    }

    fun addSticker(sticker: BaseSticker): GroupFilter {
        addToInitList(sticker)
        return this
    }

    fun removeFilter(filter: BaseFilter) {
        synchronized(filters) {
            filters.remove(filter)
        }
        updateLink()
        addToReleaseList(filter)
    }

    fun removeSticker(sticker: BaseSticker) {
        synchronized(stickers) {
            stickers.remove(sticker)
        }
        addToReleaseList(sticker)
    }

    private fun printLink() {
        val bf = StringBuffer("CAMERA->")
        filters.forEach {
            bf.append("${it::class.java.simpleName}(${it.textureId[0]}, " +
                    "${it.frameBuffer[0]}, ${it.frameBufferTexture[0]})->")
        }
        bf.append("END")
        debug_i("Link: $bf")
    }

    private fun updateLink() {
        synchronized(filters) {
            filters.first().textureId = this.textureId
            frameBuffer[0] = filters.first().frameBuffer[0]
            var lastFrameBufferTexture: IntArray = filters.first().frameBufferTexture
            filters.forEachIndexed { index, filter ->
                //不是第一个
                if (0 != index) {
                    filter.textureId = lastFrameBufferTexture
                    lastFrameBufferTexture = filter.frameBufferTexture
                }
            }
            if (null == lastFilter) {
                frameBufferTexture[0] = lastFrameBufferTexture[0]
            } else {
                lastFilter?.textureId = lastFrameBufferTexture
                frameBufferTexture[0] = lastFilter!!.frameBufferTexture[0]
            }
            synchronized(stickers) {
                stickers.forEach {
                    it.textureId = filters.last().frameBuffer
                }
            }
            if (BuildConfig.DEBUG) {
                printLink()
            }
        }
    }

    private fun notContain(list: LinkedBlockingQueue<*>, e: Any): Boolean {
        return !list.contains(e)
    }

    private fun addToInitList(texture: BaseTexture) {
        synchronized(initList) {
            if (notContain(initList, texture))
                initList.offer(texture)
        }
    }

    private fun addToReleaseList(texture: BaseTexture) {
        synchronized(releaseList) {
            if (notContain(releaseList, texture))
                releaseList.offer(texture)
        }
    }

    private fun initDeFlashing() {
        if (deFlashing && null == lastFilter) {
            lastFilter = NormalFilter(width, height, textureId)
            lastFilter?.init()
        }
    }

    /**
     * 防止集中卡顿，每帧只初始化一个
     */
    private fun initList() {
        if (initList.isEmpty()) return
        if (width <= 0 || height <= 0) throw RuntimeException("Width and height cannot be 0")
        synchronized(initList) {
            val texture = initList.poll()
            when (texture) {
                is BaseFilter -> synchronized(filters) {
                    texture.width = this.width
                    texture.height = this.height
                    texture.init()
                    filters.add(texture)
                    updateLink()
                }
                is BaseSticker -> synchronized(stickers) {
                    texture.width = this.width
                    texture.height = this.height
                    texture.init()
                    stickers.add(texture)
                    updateLink()
                }
                else -> {
                    texture.release()
                }
            }
        }
    }

    /**
     * 防止集中卡顿，每帧只释放一个
     */
    private fun releaseList() {
        if (releaseList.isEmpty()) return
        synchronized(releaseList) {
            val texture = releaseList.poll()
            texture.release()
        }
    }

    override fun init() {
        if (width <= 0 || height <= 0) throw RuntimeException("Width and height cannot be 0")
        initDeFlashing()
        initList()
    }

    override fun draw(transformMatrix: FloatArray?) {
        initList()
        releaseList()
        drawFilters()
        drawStickers()
        lastFilter?.draw(null)
    }

    private fun drawFilters() {
        synchronized(filters) {
            if (filters.isEmpty()) return
            filters.forEach {
                it.draw(null)
            }
        }
    }

    private fun drawStickers() {
        synchronized(stickers) {
            if (stickers.isEmpty()) return
            stickers.forEach {
                it.draw(null)
            }
        }
    }

    private fun releaseFilters() {
        synchronized(filters) {
            if (filters.isEmpty()) return
            filters.forEach {
                it.release()
            }
            filters.clear()
        }
    }

    private fun releaseStickers() {
        synchronized(stickers) {
            if (stickers.isEmpty()) return
            stickers.forEach {
                it.release()
            }
            stickers.clear()
        }
    }

    override fun updateFrameBuffer(width: Int, height: Int) {
        synchronized(filters) {
            if (filters.isEmpty()) return
            filters.forEach {
                it.updateFrameBuffer(width, height)
            }
            lastFilter?.updateFrameBuffer(width, height)
        }
        synchronized(stickers) {
            if (stickers.isEmpty()) return
            stickers.forEach {
                it.updateSize(width, height)
            }
        }
    }

    override fun release() {
        releaseStickers()
        releaseFilters()
        lastFilter?.release()
    }

    /**
     * Not use
     */
    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    /**
     * Not use
     */
    override fun getFragment(): String {
        return "shader/fragment_normal.glsl"
    }

    companion object {
        /**
         * Create a GroupFilter.
         * @param filter At least one filter.
         * @param deFlashing If the sticker is flashing, please open this.Performance may drop slightly
         */
        fun create(filter: BaseFilter, deFlashing: Boolean = true): GroupFilter {
            return GroupFilter(filter, deFlashing)
        }
    }
}