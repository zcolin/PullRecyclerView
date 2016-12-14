ZRecyclerViewLib
=
### RecyclerView的下拉刷新到底加载的库，基于[SuperRecycleView](https://github.com/supercwn/SuperRecycleView)修改，<br>加载效果使用[AVLoadingIndicatorView](https://github.com/81813780/AVLoadingIndicatorView),特此感谢

1. 制定自定义样式加载Footer请实现BaseLoadMoreFooter，参照DefLoadMoreFooter.
2. 制定自定义样式下拉Header请实现BaseRefreshHeader，参照DefRefreshHeader.
3. 可以设置HeaderView、FooterView、emptypView、下拉样式、加载样式等操作.
4. 所有设置在ZRecyclerView中操作，不再在Aaapter中进行操作.

## Gradle
app的build.gradle中添加
```
dependencies {
    compile 'com.github.zcolin:zcolin_ZRecyclerViewLib:1.0.0'
}
```
工程的build.gradle中添加
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

usage
=

//设置HeaderView
zRecyclerView.setHeaderView(view);


