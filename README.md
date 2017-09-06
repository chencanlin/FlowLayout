# FlowLayout #

相关功能：

	1、适配器模式填充数据；
	2、支持多个按钮同时选中；
    3、按钮点击回调；
    4、支持数据更改刷新view；

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
		mFL.setAdapter(new MyAdapter());

		private class MyAdapter extends FlowLayout.BaseAdapter<String>{
        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public View getView(int position, ViewGroup parent) {
            TextView textView = new TextView(parent.getContext());
            textView.setPadding(DensityUtils.dp2px(MainActivity.this, 5), DensityUtils.dp2px(MainActivity.this, 5), DensityUtils.dp2px(MainActivity.this, 5), DensityUtils.dp2px(MainActivity.this, 5));
            textView.setBackground(getResources().getDrawable(R.drawable.selector_flow_layout_tv_bg));
            textView.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
            textView.setTextSize(13);
            textView.setText(data.get(position));
            return textView;
        }
    }


		// 可以获取点击监听，也可以自己在adapter里面实现点击监听
		mFL.setOnClickListener(new OnFlowLayoutClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, ((TextView) v).getText().toString()+ "------" + v.getId(), Toast.LENGTH_SHORT).show();
            }
        });

		更新view：
		mFL.notifyDataSetChanged();


