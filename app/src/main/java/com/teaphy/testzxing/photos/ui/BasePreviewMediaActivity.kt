package com.teaphy.testzxing.photos.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.rrs.afcs.picture.PictureHelper
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.constant.PhotosConstant
import com.teaphy.testzxing.photos.entity.LocalMedia

abstract class BasePreviewMediaActivity : BasePhotosActivity() {

    protected lateinit var mediaViewPager: ViewPager
    protected lateinit var backLayout: View
    protected lateinit var numText: TextView
    protected lateinit var percentText: TextView
    private lateinit var selectButton: Button
    protected lateinit var imageTab: TabLayout

    protected val listImage = mutableListOf<LocalMedia>()
    protected val listImageSelected = mutableListOf<LocalMedia>()

    protected val mediaAdapter = SingleMediaAdapter(listImage)

    private var indexPreview = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_midia_layout)

        initView()

        setListener()

        loadMediaData()

        updateSelectButtonUI()

        defineTabItem()

        setCurrentItem(indexPreview)
    }

    private fun initView() {
        mediaViewPager = findViewById(R.id.mediaViewPager)
        backLayout = findViewById(R.id.backLayout)
        numText = findViewById(R.id.numText)
        percentText = findViewById(R.id.percentText)
        selectButton = findViewById(R.id.selectButton)
        imageTab = findViewById(R.id.imageTab)

        mediaViewPager.adapter = mediaAdapter

        imageTab.tabMode = TabLayout.MODE_SCROLLABLE
    }

    open fun setListener() {

        backLayout.setOnClickListener {
            finish()
        }

        mediaViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateSelectChangeUI(position)
            }
        })



        imageTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val position = it.position
                    updateTabUI(it, listImageSelected[position], false)
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val position = it.position

                    val localMedia = listImageSelected[position]
                    val posAll = listImage.indexOf(localMedia)
                    mediaViewPager.currentItem = posAll

                    updateTabUI(it, listImageSelected[position], true)
                }
            }

        })
    }

    private fun loadMediaData() {
        val bundle = intent.extras

        val images = bundle.getParcelableArrayList<LocalMedia>(PhotosConstant.KEY_CONTENT)
        val imagesSelected = bundle.getParcelableArrayList<LocalMedia>(PhotosConstant.KEY_MEDIA_SELECTED)
        indexPreview = bundle.getInt(PhotosConstant.KEY_POSITION, 0)

        imagesSelected?.let {
            if (it.isNotEmpty()) {
                listImageSelected.addAll(it)
            }
        }

        images?.let {
            if (it.isNotEmpty()) {
                listImage.addAll(it)
            }
        }

        mediaAdapter.notifyDataSetChanged()
    }


    private fun defineTabItem() {

        listImageSelected.forEach { localMedia ->
            imageTab.addTab(updateTabUI(imageTab.newTab(), localMedia, false), false)
        }
    }

    override fun finish() {

        setMediaResult()

        super.finish()
    }

    /**
     * 创建 选择的图片Result
     */
    private fun setMediaResult() {

        val listSelect = listImageSelected.filter {
            it.isChecked
        } as ArrayList<LocalMedia>
        val intent = Intent()
        val bundle = Bundle()

        bundle.putParcelableArrayList(PhotosConstant.KEY_CONTENT, listSelect)

        intent.putExtras(bundle)
        setResult(Activity.RESULT_OK, intent)
    }

    protected fun setCurrentItem(position: Int) {
        mediaViewPager.setCurrentItem(position, false)

        updateSelectChangeUI(position)
    }

    fun updateNumUI(localMedia: LocalMedia) {

        if (localMedia.isChecked) {
            numText.setBackgroundResource(R.mipmap.ic_pigeon_selected)
            val pos = listImageSelected.asSequence().filter {
                it.isChecked
            }.indexOf(localMedia)
            numText.text = (pos + 1).toString()
        } else {
            numText.setBackgroundResource(R.mipmap.ic_pigeon)
            numText.text = null
        }
    }

    fun updateSelectButtonUI() {

        val countSelect = listImageSelected.filter { it.isChecked }.size

        val selectDesc = if (countSelect > 0) {
            getString(R.string.photos_select_num, countSelect)
        } else {
            getString(R.string.select)
        }

        selectButton.text = selectDesc
    }



    fun updateTabUI(tab: TabLayout.Tab, localMedia: LocalMedia, isSelected: Boolean): TabLayout.Tab {

        tab.let {
            var view = it.customView
            if (view == null) {
                view = LayoutInflater.from(this)
                        .inflate(R.layout.tab_item_preview_media, null, false)
                it.customView = view
            }
            val image = view!!.findViewById<ImageView>(R.id.image)
            val squareView = view.findViewById<View>(R.id.squareView)
            if (null != image) {
                PictureHelper().loadLocalImage(image, localMedia.path)

                if (localMedia.isChecked) {
                    image.alpha = 1.0f
                } else {
                    image.alpha = 0.7f
                }
            }

            if (null != squareView) {
                squareView.visibility = if (isSelected) View.VISIBLE else View.GONE
            }
        }
        return tab
    }

    abstract fun updateSelectChangeUI(position: Int)
}
