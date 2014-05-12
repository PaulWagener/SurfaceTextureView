package nl.paulwagener;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL11;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;

/**
 * A subclass of TextureView, but with methods added that make it function like
 * a GLSurfaceView. Instances of GLSurfaceView.Renderer can be supplied via the
 * setRenderer() interface.
 * 
 * Based on
 * https://github.com/dalinaum/TextureViewDemo/blob/master/src/kr/gdg/android
 * /textureview/GLTriangleActivity.java
 */
@SuppressLint("NewApi")
public class GLTextureView extends TextureView implements
SurfaceTextureListener, GLView {

	public final int FPS = 60;
	private RenderThread renderThread;
	GLSurfaceView.Renderer renderer;

	public GLTextureView(Context context) {
		super(context);
		init();
	}

	public GLTextureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GLTextureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setSurfaceTextureListener(this);
		setOpaque(false);
	}

	@Override
	public void setRenderer(GLSurfaceView.Renderer renderer) {
		this.renderer = renderer;
		if (renderThread != null) {
			renderThread.renderer = renderer;
		}

	}

	private class RenderThread extends Thread {
		private Renderer renderer;
		private static final int EGL_OPENGL_ES2_BIT = 4;
		private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
		private static final String TAG = "RenderThread";
		private final SurfaceTexture mSurface;
		private EGLDisplay mEglDisplay;
		private EGLSurface mEglSurface;
		private EGLContext mEglContext;
		private EGL10 mEgl;
		private GL11 mGl;
		private EGLConfig eglConfig;
		private Point newSize;
		private final Queue<Runnable> queue = new LinkedList<Runnable>();

		public RenderThread(SurfaceTexture surface) {
			mSurface = surface;
		}

		@Override
		public void run() {
			initGL();

			if (renderer != null) {
				renderer.onSurfaceCreated(mGl, eglConfig);
			}

			while (true) {
				checkCurrent();

				synchronized (queue) {
					while (!queue.isEmpty()) {
						queue.poll().run();
					}
				}

				if (newSize != null && renderer != null) {
					renderer.onSurfaceChanged(mGl, newSize.x, newSize.y);
					newSize = null;
				}

				renderer.onDrawFrame(mGl);

				if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
					Log.e(TAG, "cannot swap buffers!");
				}
				checkEglError();

				try {
					Thread.sleep(1000 / FPS);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private void checkCurrent() {
			if (!mEglContext.equals(mEgl.eglGetCurrentContext())
					|| !mEglSurface.equals(mEgl
							.eglGetCurrentSurface(EGL10.EGL_DRAW))) {
				checkEglError();
				if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface,
						mEglContext)) {
					throw new RuntimeException("eglMakeCurrent failed "
							+ GLUtils.getEGLErrorString(mEgl.eglGetError()));
				}
				checkEglError();
			}
		}

		private void checkEglError() {
			final int error = mEgl.eglGetError();
			if (error != EGL10.EGL_SUCCESS) {
				Log.e(TAG, "EGL error = 0x" + Integer.toHexString(error));
			}
		}

		/**
		 * Lots of boilerplate to create a GL context
		 */
		private void initGL() {
			mEgl = (EGL10) EGLContext.getEGL();

			mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
			if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
				throw new RuntimeException("eglGetDisplay failed "
						+ GLUtils.getEGLErrorString(mEgl.eglGetError()));
			}

			int[] version = new int[2];
			if (!mEgl.eglInitialize(mEglDisplay, version)) {
				throw new RuntimeException("eglInitialize failed "
						+ GLUtils.getEGLErrorString(mEgl.eglGetError()));
			}

			int[] configsCount = new int[1];
			EGLConfig[] configs = new EGLConfig[1];
			int[] configSpec = { EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
					EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8,
					EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE, 8,
					EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_STENCIL_SIZE, 0,
					EGL10.EGL_NONE };

			eglConfig = null;
			if (!mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 1,
					configsCount)) {
				throw new IllegalArgumentException("eglChooseConfig failed "
						+ GLUtils.getEGLErrorString(mEgl.eglGetError()));
			} else if (configsCount[0] > 0) {
				eglConfig = configs[0];
			}
			if (eglConfig == null) {
				throw new RuntimeException("eglConfig not initialized");
			}

			int[] attrib_list = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
			mEglContext = mEgl.eglCreateContext(mEglDisplay, eglConfig,
					EGL10.EGL_NO_CONTEXT, attrib_list);
			checkEglError();
			mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, eglConfig,
					mSurface, null);
			checkEglError();
			if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
				int error = mEgl.eglGetError();
				if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
					Log.e(TAG,
							"eglCreateWindowSurface returned EGL10.EGL_BAD_NATIVE_WINDOW");
					return;
				}
				throw new RuntimeException("eglCreateWindowSurface failed "
						+ GLUtils.getEGLErrorString(error));
			}

			if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface,
					mEglContext)) {
				throw new RuntimeException("eglMakeCurrent failed "
						+ GLUtils.getEGLErrorString(mEgl.eglGetError()));
			}
			checkEglError();

			mGl = (GL11) mEglContext.getGL();
			checkEglError();
		}
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		renderThread = new RenderThread(surface);
		renderThread.renderer = renderer;
		renderThread.start();
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		renderThread.interrupt();
		try {
			renderThread.join();
		} catch (InterruptedException e) {
			Log.e("Regenmelding", "Interrupted in onSurfaceTextureDestroyed", e);
		}
		renderThread = null;
		return true;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		renderThread.newSize = new Point(width, height);
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

	@Override
	public void queueEvent(Runnable runnable) {
		if (renderThread != null) {
			synchronized (renderThread.queue) {
				renderThread.queue.add(runnable);
			}
		}
	}

	@Override
	public View getView() {
		return this;
	}
}
