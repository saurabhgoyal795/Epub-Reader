/*
The MIT License (MIT)

Copyright (c) 2013, V. Giacometti, M. Giuriato, B. Petrantuono

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package it.angrydroids.epub3reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import junit.framework.Assert;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Base64;
import android.util.Log;
import android.util.Property;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

// Panel specialized in visualizing EPUB pages
public class BookView extends SplitPanel {
	public ViewStateEnum state = ViewStateEnum.books;
	protected String viewedPage;
	protected WebView view;
	protected float swipeOriginX, swipeOriginY;
	 private ActionMode.Callback mActionModeCallback;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)	{
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.activity_book_view, container, false);
		return v;
	}
	
	@Override
    public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		view = (WebView) getView().findViewById(R.id.Viewport);
		
		// enable JavaScript for cool things to happen!
		view.getSettings().setJavaScriptEnabled(true);
		WebView.setWebContentsDebuggingEnabled(true);
		view.getSettings().setAllowUniversalAccessFromFileURLs(true);
		WebBrowserJSInterface k=new WebBrowserJSInterface();
        view.addJavascriptInterface(k,
			 		"Android");
		
		// ----- SWIPE PAGE
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
	
				if (state == ViewStateEnum.books)
					swipePage(v, event, 0);
								
				
				WebView view = (WebView) v;
				
				return view.onTouchEvent(event);
			}
		});
		view.setLongClickable(true);
		// ----- NOTE & LINK
		view.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				    
				
				
		//	emulateShiftHeld(view);
				 
//				        ActionMode mActionMode = null;
//						if (mActionMode != null) {
//				            return false;
//				        }
//
//				        // Start the CAB using the ActionMode.Callback defined above
//					    mActionModeCallback = new CustomActionModeCallback();
//						Log.v("tag","avtion mode"+mActionModeCallback);
//
//				        mActionMode = getView().startActionMode(mActionModeCallback);
//			        v.setSelected(true);
//					Message msg = new Message();
//
//					msg.setTarget(new Handler() {
//						@Override
//						public void handleMessage(Message msg) {
//							super.handleMessage(msg);
//
//							String url = msg.getData().getString(
//									getString(R.string.url));
//							Log.v("tag","value is "+url);
//							if (url != null)
//								navigator.setNote(url, index);
//						}
//					});
//					view.requestFocusNodeHref(msg);
				
				return true;
			}

			
		});
		
		view.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				try {
					navigator.setBookPage(url, index);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_LoadPage));
				}
				return true;
				
			}
			 
		});
		
		loadPage(viewedPage);
	}
	
	public void loadPage(String path)
	{
		viewedPage = path;
		if(created)
			view.loadUrl(path);
	}
	
	private void emulateShiftHeld(WebView view)
    {
        try
        {
            KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                                                    KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
            shiftPressEvent.dispatch(view);
            Log.v("tag","shift"+shiftPressEvent.getCharacters());
            Toast.makeText(getActivity(), "select_text_now", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Log.e("dd", "Exception in emulateShiftHeld()", e);
        }
    }
	
	// Change page
	protected void swipePage(View v, MotionEvent event, int book) {
		int action = MotionEventCompat.getActionMasked(event);

		switch (action) {
		case (MotionEvent.ACTION_DOWN):
			swipeOriginX = event.getX();
			swipeOriginY = event.getY();
			break;

		case (MotionEvent.ACTION_UP):
			int quarterWidth = (int) (screenWidth * 0.25);
			float diffX = swipeOriginX - event.getX();
			float diffY = swipeOriginY - event.getY();
			float absDiffX = Math.abs(diffX);
			float absDiffY = Math.abs(diffY);

			if ((diffX > quarterWidth) && (absDiffX > absDiffY)) {
				try {
					navigator.goToNextChapter(index);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_cannotTurnPage));
				}
			} else if ((diffX < -quarterWidth) && (absDiffX > absDiffY)) {
				try {
					navigator.goToPrevChapter(index);
				} catch (Exception e) {
					errorMessage(getString(R.string.error_cannotTurnPage));
				}
			}
			break;
		}

	}
	
	@Override
	public void saveState(Editor editor) {
		super.saveState(editor);
		editor.putString("state"+index, state.name());
		editor.putString("page"+index, viewedPage);
	}
//    private void injectScriptFile(WebView view, String scriptFile) {
//        InputStream input;
//        try {
//        
//           input = getAssets().open(scriptFile);
//           byte[] buffer = new byte[input.available()];
//           input.read(buffer);
//           input.close();
//
//           // String-ify the script byte-array using BASE64 encoding !!!
//           String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
//           view.loadUrl("javascript:(function() {" +
//                        "var parent = document.getElementsByTagName('head').item(0);" +
//                        "var script = document.createElement('script');" +
//                        "script.type = 'text/javascript';" +
//           // Tell the browser to BASE64-decode the string into your script !!!
//                        "script.innerHTML = window.atob('" + encoded + "');" +
//                        "parent.appendChild(script)" +
//                        "})()");
//        } catch (IOException e) {
//           // TODO Auto-generated catch block
//           e.printStackTrace();
//        }
//     }
//  

	@Override
	public void loadState(SharedPreferences preferences)
	{
		super.loadState(preferences);
		loadPage(preferences.getString("page"+index, ""));
		state = ViewStateEnum.valueOf(preferences.getString("state"+index, ViewStateEnum.books.name()));
	}
	private class CustomActionModeCallback implements ActionMode.Callback {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown.
        // Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Note: This is called every time the selection handlebars move.
        	
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
            case R.id.copy:
            	view.loadUrl("javascript: " +
					 		" function temp(){" +
				 		"var text=\"\";" +
				 				"if(window.getSelection)" +
				 				"{text=window.getSelection().toString();}else if (document.selection && document.selection.type!=\"Control\"){text = document.selection.createRange().text;}Android.setSelectedWord('\"' +text+ '\"');return text;}; temp();");
            	 
				 //					view.loadUrl("javascript:"+getActivity().getAssets().open("selection.js"));
                
//            	 String p = k.getWord();
//            	 Log.v("tag", "p:"+p);
//            	 Toast.makeText(getActivity(), p, Toast.LENGTH_LONG).show();
                mode.finish(); // Action picked, so close the CAB
                return true;
           
            default:
                mode.finish();
                return false;
        }

        // Called when the user exits the action mode
       
    }

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			// TODO Auto-generated method stub
			
		}	
}
	public class WebBrowserJSInterface {
		String text;

		@JavascriptInterface
		public String getWord() {
       	 Log.v("tag", "hi get Words called");

			return text;	
			
		}
		
		@JavascriptInterface
		public void setSelectedWord(String text) {
			this.text = text;
			Log.v("tag","valueof the text on selected"+text);
		}
    }
}
