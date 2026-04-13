package com.example.newapp.ui.atlas

import androidx.annotation.DrawableRes
import com.example.newapp.R

data class AtlasNodePhoto(
    @param:DrawableRes val drawableRes: Int,
    val credit: String,
    val license: String,
    val sourceUrl: String
)

object AtlasNodePhotoRegistry {
    private val photos = mapOf(
        "barnaul" to AtlasNodePhoto(
            drawableRes = R.drawable.atlas_barnaul,
            credit = "Vyacheslav Bukharov / Wikimedia Commons",
            license = "CC BY-SA 4.0",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Barnaul_(2021)-1.jpeg"
        ),
        "biysk" to AtlasNodePhoto(
            drawableRes = R.drawable.atlas_biysk,
            credit = "Takok / Wikimedia Commons",
            license = "Public domain",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Biysk_-_Panorama.jpg"
        ),
        "belokurikha" to AtlasNodePhoto(
            drawableRes = R.drawable.atlas_belokurikha,
            credit = "Антон Денисенко / Wikimedia Commons",
            license = "CC BY-SA 3.0",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:%D0%91%D0%B5%D0%BB%D0%BE%D0%BA%D1%83%D1%80%D0%B8%D1%85%D0%B0,_%D0%90%D0%BB%D1%82%D0%B0%D0%B9%D1%81%D0%BA%D0%B8%D0%B9_%D0%BA%D1%80%D0%B0%D0%B9._%D0%92%D0%B8%D0%B4_%D0%BD%D0%B0_%D0%B3%D0%BE%D1%80%D0%BE%D0%B4_01.jpg"
        ),
        "denisova_cave" to AtlasNodePhoto(
            drawableRes = R.drawable.atlas_denisova_cave,
            credit = "Yuriy59 / Wikimedia Commons",
            license = "CC BY-SA 3.0",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Denisova_cave_02.jpg"
        ),
        "tigirek" to AtlasNodePhoto(
            drawableRes = R.drawable.atlas_tigirek,
            credit = "Evgeny Boginsky / Wikimedia Commons",
            license = "CC BY-SA 4.0",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:DSC_0051_%D0%92%D0%B8%D0%B4_%D0%BD%D0%B0_%D0%A2%D0%B8%D0%B3%D0%B8%D1%80%D0%B5%D1%86%D0%BA%D0%B8%D0%B9_%D1%85%D1%80%D0%B5%D0%B1%D0%B5%D1%82.jpg"
        ),
        "chuysky_tract" to AtlasNodePhoto(
            drawableRes = R.drawable.atlas_chuysky_tract,
            credit = "Alexandr frolov / Wikimedia Commons",
            license = "CC BY-SA 4.0",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Road_number_256_or_Chuysky_Tract.jpg"
        ),
        "kulunda" to AtlasNodePhoto(
            drawableRes = R.drawable.atlas_kulunda,
            credit = "lis80 / Wikimedia Commons",
            license = "CC BY 3.0",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:%D0%9E%D0%B7%D0%B5%D1%80%D0%BE_%D0%BA%D1%83%D0%BB%D1%83%D0%BD%D0%B4%D0%B8%D0%BD%D1%81%D0%BA%D0%BE%D0%B5_-_panoramio.jpg"
        ),
        "kolyvan" to AtlasNodePhoto(
            drawableRes = R.drawable.atlas_kolyvan,
            credit = "Kidus22 / Wikimedia Commons",
            license = "CC BY-SA 4.0",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:200_%D0%BB%D0%B5%D1%82_%D0%9A%D0%BE%D0%BB%D1%8B%D0%B2%D0%B0%D0%BD%D1%81%D0%BA%D0%BE%D0%BC%D1%83_%D0%BA%D0%B0%D0%BC%D0%BD%D0%B5%D1%80%D0%B5%D0%B7%D0%BD%D0%BE%D0%BC%D1%83_%D0%B7%D0%B0%D0%B2%D0%BE%D0%B4%D1%83.jpg"
        )
    )

    fun photoFor(nodeId: String): AtlasNodePhoto? = photos[nodeId]
}
