/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.filter

import com.lmy.codec.helper.GLHelper
import com.lmy.codec.texture.impl.BaseTexture
import com.lmy.codec.texture.impl.sticker.BaseSticker
import com.lmy.codec.util.debug_i
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by lmyooyo@gmail.com on 2018/9/30.
 */
class GroupFilter private constructor(filter: BaseFilter,
                                      width: Int = 0,
                                      height: Int = 0,
                                      textureId: IntArray = IntArray(1))
    : BaseFilter(width, height, textureId) {
    private val initList = LinkedBlockingQueue<BaseTexture>()
    private val releaseList = LinkedBlockingQueue<BaseTexture>()
    private val filters = ArrayList<BaseFilter>()
    private val stickers = ArrayList<BaseSticker>()

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
        val bf = StringBuffer()
        filters.forEach {
            bf.append(it.textureId[0])
                    .append("->")
                    .append(it.frameBuffer[0])
                    .append("(${it.frameBufferTexture[0]}), ")
        }
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
            frameBufferTexture[0] = lastFrameBufferTexture[0]
            printLink()
        }
        synchronized(stickers) {
            stickers.forEach {
                it.textureId = frameBufferTexture
            }
        }
    }

    private fun addToInitList(texture: BaseTexture) {
        synchronized(initList) {
            initList.offer(texture)
        }
    }

    private fun addToReleaseList(texture: BaseTexture) {
        synchronized(releaseList) {
            releaseList.offer(texture)
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
            if (texture is BaseFilter) {
                texture.width = this.width
                texture.height = this.height
            }
            texture.init()
            when (texture) {
                is BaseFilter -> synchronized(filters) {
                    filters.add(texture)
                    updateLink()
                }
                is BaseSticker -> synchronized(stickers) {
                    stickers.add(texture)
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
        initList()
    }

    override fun draw(transformMatrix: FloatArray?) {
        initList()
        releaseList()
        GLHelper.checkGLES2Error("GroupFilter releaseList")
        drawFilters()
        GLHelper.checkGLES2Error("GroupFilter drawFilters")
        drawStickers()
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

    override fun release() {
        releaseStickers()
        releaseFilters()
    }

    override fun getVertex(): String {
        return "shader/vertex_normal.glsl"
    }

    override fun getFragment(): String {
        return "shader/fragment_normal.glsl"
    }

    companion object {
        fun create(filter: BaseFilter): GroupFilter {
            return GroupFilter(filter)
        }
    }
}