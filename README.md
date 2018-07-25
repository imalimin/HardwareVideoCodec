# HardwareVideoCodec
HardwareVideoCodec is an efficient video encoding library for Android.With it you can easily record videos of various resolutions on your android app.

## Latest release
[V1.4.1](https://github.com/lmylr/HardwareVideoCodec/releases/tag/v1.4.1)

* Support for changing resolution without restarting the camera.
* Fix audio distortion.
* Supports 20 filters.

## Features
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
    implementation 'com.lmy.codec:hardwarevideocodec:1.4.1'
}
```
* MainActivity
```
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mTextureView = TextureView(this)
        setContentView(mTextureView)
        val mPresenter = RecordPresenter(CodecContext(this).apply {
            ioContext.path = "${Environment.getExternalStorageDirectory().absolutePath}/test.mp4"
        })
        mTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                mPresenter.updatePreview(width, height)
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                mPresenter.stopPreview()
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                if (null != surface)
                    mPresenter.startPreview(surface, width, height)
            }
        }
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
}
```
## Join the HardwareVideoCodec community
Please use our [issues page](https://github.com/lmylr/HardwareVideoCodec/issues) to let us know of any problems.

## License
HardwareVideoCodec is [GPL-licensed](https://github.com/lmylr/HardwareVideoCodec/tree/master/LICENSE).