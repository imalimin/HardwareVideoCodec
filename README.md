# HWVC(HardwareVideoCodec)
[ ![Download](https://api.bintray.com/packages/lmylr/maven/hardwarevideocodec/images/download.svg) ](https://bintray.com/lmylr/maven/hardwarevideocodec/_latestVersion)

HWVC is an efficient video encoding library for Android. Supports `software` and `hardware` encode.
With it, you can encode a video at any resolution, no longer need to care about camera resolution. Everything is so easy.

![ScreenRecord_1](https://github.com/lmylr/HardwareVideoCodec/blob/master/images/ScreenRecord_1.gif)
![ScreenRecord_1](https://github.com/lmylr/HardwareVideoCodec/blob/master/images/ScreenRecord_2.gif)
## Latest release
[V1.7.1](https://github.com/lmylr/HardwareVideoCodec/releases/tag/v1.7.1)

* Fix after switched the filter, the video of some devices will become black.

## Features
* Support video encoding at any resolution. No need to care about camera resolution.
* Support RTMP stream.
* Support for changing resolution without restarting the camera.
* Support hard & soft encode.
* Record video & audio. Pack mp4 through MediaMuxer.
* Use OpenGL to render and support filter.
* Supports 40+ filters
* Support beauty filter.
* Support for changing resolution without restarting the camera.
* More features.

## Start
If you are building with Gradle, simply add the following code to your project:
* You need to configure your kotlin environment. Project root build.gradle
```
buildscript {
    ext.kotlin_version = '1.2.30'//Latest kotlin version
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}
allprojects {
    repositories {
        google()
        jcenter()
    }
}
```
* Module build.gradle
```
dependencies {
    implementation 'com.lmy.codec:hardwarevideocodec:1.7.1'
    implementation 'com.lmy.codec:rtmp:1.7.1'//If you want to use RTMP stream.
}
```
* For record mp4
```
class MainActivity : AppCompatActivity() {
    private lateinit var mRecorder: VideoRecorderImpl
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mTextureView = TextureView(this)
        setContentView(mTextureView)
        mRecorder = VideoRecorderImpl(this).apply {
            reset()
            setOutputUri("${Environment.getExternalStorageDirectory().absolutePath}/test.mp4")
            setOutputSize(720, 1280)//Default 720x1280
            setFps(30)
            setFilter(getDefaultFilter())
            setPreviewDisplay(mTextureView)
        }
        mRecorder.prepare()
        //For recording control
        mTextureView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (mRecorder.prepared())
                        mRecorder.start()
                }
                MotionEvent.ACTION_UP -> {
                    if (mRecorder.started())
                        mRecorder.pause()
                }
            }
            true
        }
    }

    private fun getDefaultFilter(): BaseFilter = GroupFilter.create(BeautyV4Filter())
            .addSticker(TextSticker().apply {
                setText(TextSticker.Text("HWVC", 56f).apply {
                    x = 0.8f
                    y = 0.03f
                })
            })
            .addSticker(ImageSticker().apply {
                setImage(ImageSticker.Image().apply {
                    x = 0.03f
                    y = 0.03f
                    scale = 1.6f
                    bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_logo_hwvc)
                })
            })

    override fun onDestroy() {
        super.onDestroy()
        mRecorder.release()
    }
}
```
* For live
```
class MainActivity : AppCompatActivity() {
    private lateinit var mRecorder: VideoRecorderImpl
    private val onStateListener = object : VideoRecorder.OnStateListener {

        override fun onStop() {
        }

        override fun onPrepared(encoder: Encoder) {
            mRecorder.start()
        }

        override fun onRecord(encoder: Encoder, timeUs: Long) {
        }

        override fun onError(error: Int, msg: String) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mTextureView = TextureView(this)
        setContentView(mTextureView)
        mRecorder = VideoRecorderImpl(this).apply {
            reset()
            setOutputUri("rtmp://192.168.16.125:1935/live/livestream")
            setOutputSize(720, 1280)//Default 720x1280
            setFps(30)
            setFilter(getDefaultFilter())//Default NormalFilter
            setPreviewDisplay(mTextureView)
            setOnStateListener(onStateListener)
        }
        mRecorder.prepare()
    }

    private fun getDefaultFilter(): BaseFilter = GroupFilter.create(BeautyV4Filter())
            .addSticker(TextSticker().apply {
                setText(TextSticker.Text("HWVC", 56f).apply {
                    x = 0.8f
                    y = 0.03f
                })
            })
            .addSticker(ImageSticker().apply {
                setImage(ImageSticker.Image().apply {
                    x = 0.03f
                    y = 0.03f
                    scale = 1.6f
                    bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_logo_hwvc)
                })
            })

    override fun onDestroy() {
        super.onDestroy()
        mRecorder.release()
    }
}
```
## Join the HWVC community
Please use our [issues page](https://github.com/lmylr/HardwareVideoCodec/issues) to let us know of any problems.

## License
HWVC(HardwareVideoCodec) is [GPL-licensed](https://github.com/lmylr/HardwareVideoCodec/tree/master/LICENSE).