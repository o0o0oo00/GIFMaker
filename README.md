# GIFMaker
仿照Google Photo 智能助理的动画效果，从相册选择几张图片生成GIF，重点是在与不同大小的图片的裁切规则

分析：
1、从相册中选取图片
2、根据Uri得到诸多的bitmap
3、裁剪bitmap，使多张图片最终大小一致
4、根据这些bitmap生成GIF

第一步、从相册中选取图片，使用知乎开源的Matisse框架.
第二步、根据返回的uri 统计bitmap的宽高比，用于调教最终的宽高比。（此问题困扰多天，终于定下来一套裁切规范）

List<Uri> uris = Matisse.obtainResult(data);
这种方式访问bitmap不会加载到内存中，仅仅读取文件信息。得到我们想要的宽和高；
BitmapFactory.Options op = new BitmapFactory.Options();
op.inJustDecodeBounds = true;

还有需要注意的是，由于有些图片像素巨大（前置摄像头拍出）有可能会是3000*4000左右，或者有可能图片太小（自己剪裁的图片）只有300*400左右。此时统一规范一下，全部都缩放到1200左右。此数值是根据Google 相册的动画效果得到的最终大小经过大量试验得到的（差不多就是1200这个数值浮动）。

裁切规范：
首先，计算图片的宽高比，如果宽高比大于1，证明是扁平形状（宽形），小于1则为长高形状（长形）。
然后，统计一组bitmap中的宽高比出现次数最多的一种宽高比值，否则取第一张图片的宽高比值，作为最终宽高比（能少裁剪一些就少裁剪一些）
最终宽高比有了，根据宽高比与1的比较可以得出它是长形还是宽形。如果是宽形则宽比较大，宽缩小到1200 ，高缩小（放大）同样的比例。反之，如果是长形则高比较大，高缩小（放大）到1200，宽缩放同样的比例。这样我们就得到了最终的GIF图片的宽和高。传入Gifflen中去。

然后，遍历所有bitmap，裁剪他们。仅仅分三种情况：1、比最终大小更宽 。2、比最终大小更高。3、与最终大小宽高比一致，只需要做缩放操作就可以了。
1、如果比最终大小还要宽，那么高度缩放到最终高度，中心裁剪宽度。
2、如果比最终大小还要高，那么宽度缩放到最终宽度，中心裁剪高度。
3、宽高比与最终一致，缩放到最终宽高大小。

裁切完毕。存储到临时文件夹，使用 ffmpeg 以命令形式生成GIF。

这里的ffmpeg已经精简编译过了，如果有需要其他的cpu架构可以联系我(￣▽￣)"


