package nl.paulwagener;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class GLLayout extends RelativeLayout {
	private GLTextureView glTextureView;

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

		glTextureView = new GLTextureView(getContext());
		addView(glTextureView);
	}
}
