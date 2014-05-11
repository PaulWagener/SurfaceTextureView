package nl.paulwagener;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * GLLayout is a generic layout that decides at runtime if it includes a
 * TextureView or a GLSurfaceView. TextureView is preferred, but if it is not
 * yet supported on the device it falls back on GLSurfaceView.
 * 
 * Currently only supports being inflated from a layout.
 */
public class GLLayout extends RelativeLayout {
	private GLView glView;

	public GLLayout(Context context) {
		super(context);
	}

	public GLLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GLLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		glView = createGLView(getContext());
		addView(glView.getView());
	}

	public GLView getGLView() {
		return glView;
	}

	/**
	 * Returns a generic 'GLView', which internally uses either a TextureView or
	 * a GLSurfaceView. TextureView is returned if it is supported, otherwise
	 * GLSurfaceView is returned
	 */
	public static GLView createGLView(Context context) {
		if (Build.VERSION.SDK_INT >= 14) {
			return new GLTextureView(context);
		} else {
			GLSurfaceView glsurfaceview = new GLSurfaceView(context);
			glsurfaceview.setEGLContextClientVersion(2);
			glsurfaceview.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			glsurfaceview.getHolder().setFormat(PixelFormat.TRANSLUCENT);
			glsurfaceview.setZOrderMediaOverlay(false);
			glsurfaceview.setZOrderOnTop(true);
			return new GLSurfaceViewWrapper(glsurfaceview);
		}
	}
}
