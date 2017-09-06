# FlowLayout #

相关功能：

	1、支持多个按钮同时选中；
    2、按钮点击回调；
    3、支持数据更改刷新view；

效果：

![](https://i.imgur.com/LeWM9dn.gif)

xml声明：

	<com.ccl.perfectisshit.flowlayout.widget.FlowLayout
        android:id="@+id/fl"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingLeft="10dp"
        android:paddingRight="10dp"
        android:paddingTop="15dp"
        android:paddingBottom="15dp"/>


代码调用：

		mFL = findViewById(R.id.fl);
		mFL.setDataList(data);
		mFL.setOnClickListener(new OnFlowLayoutClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, ((TextView) v).getText().toString()+ "------" + v.getId(), Toast.LENGTH_SHORT).show();
            }
        });

		更新view：
		mFL.notifyDataSetChanged();


