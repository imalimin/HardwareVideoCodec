# HardwareVideoCodec
[ ![Download](https://api.bintray.com/packages/lmylr/maven/hardwarevideocodec/images/download.svg) ](https://bintray.com/lmylr/maven/hardwarevideocodec/_latestVersion)

HardwareVideoCodec is an efficient video encoding library for Android. Supports `software` and `hardware` encode.
With it, you can encode a video at any resolution, no longer need to care about camera resolution. Everything is so easy.

![ScreenRecord_1](https://github.com/lmylr/HardwareVideoCodec/blob/master/images/ScreenRecord_1.gif)
![ScreenRecord_1](https://github.com/lmylr/HardwareVideoCodec/blob/master/images/ScreenRecord_2.gif)
## Latest release
[V1.5.2](https://github.com/lmylr/HardwareVideoCodec/releases/tag/v1.5.2)

* Support video encoding at any resolution. No need to care about camera resolution.
* RTMP module supports caching, and automatic frame dropping strategy.
* Fixed the problem of restarting the app without a picture.
* Fixed a bug in RTMP connection timeout setting error.

## Features
* Support video encoding at any resolution. No need to care about camera resolution.
* Support RTMP stream.
* Support for changing resolution without restarting the camera.
* Support hard & soft encode.
* Record video & audio. Pack mp4 through MediaMuxer.
* Use OpenGL to render and support filter.
* Supports 20 filters
* Support beauty filter.
* Support for changing resolution without restarting the camera.
* More features.

## Start
If you are building with Gradle, simply add the following code to your project:
* Project root build.gradle
```
buildscript {
    ext.kotlin_version = '1.2.30'//Latest kotlin version
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}
allprojects {
    repositories {
        maven {
            url 'https://dl.bintray.com/lmylr/maven'
        }
    }
}
```
* Module build.gradle
```
dependencies {
    implementation 'com.lmy.codec:hardwarevideocodec:1.5.2'
    implementation 'com.lmy.codec:rtmp:1.1.0'//If you want to use RTMP stream.
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
        mPresenter = RecordPresenter(CodecContext(this).apply {
            ioContext.path = "${Environment.getExternalStorageDirectory().absolutePath}/test.mp4"
            //ioContext.path = "rtmp://192.168.16.203:1935/live/livestream"//If you want to use RTMP stream.
        })
        mPresenter.setPreviewTexture(mTextureView)
        //For recording control
        mTextureView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mPresenter.encoder?.start()
                    mPresenter.audioEncoder?.start()
                }
                MotionEvent.ACTION_UP -> {
                    mPresenter.encoder?.pause()
                    mPresenter.audioEncoder?.pause()
                }
            }
            true
        }
    }
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mTextureView = TextureView(this)
        setContentView(mTextureView)
        mRecorder = VideoRecorderImpl(this).apply {
            reset()
            setOutputUri("rtmp://192.168.16.125:1935/live/livestream")
            setOutputSize(720, 1280)//Default 720x1280
            setFilter(NormalFilter::class.java)//Default NormalFilter
            setPreviewDisplay(mTextureView)
            setOnStateListener(onStateListener)
        }
        mRecorder.prepare()
        mRecorder.setOnStateListener(object : RecordPresenter.OnStateListener {
            override fun onStop() {
            }

            override fun onPrepared(encoder: Encoder) {
                mRecorder.start()
            }

            override fun onRecord(encoder: Encoder, timeUs: Long) {
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        mRecorder.release()
    }
}
```
## Join the HardwareVideoCodec community
Please use our [issues page](https://github.com/lmylr/HardwareVideoCodec/issues) to let us know of any problems.

## License
HardwareVideoCodec is [GPL-licensed](https://github.com/lmylr/HardwareVideoCodec/tree/master/LICENSE).