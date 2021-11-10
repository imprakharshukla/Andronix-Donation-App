package com.techriz.andronix.donation.utils

import com.techriz.andronix.donation.R

sealed class SkuInfo {
    //primus, blaze, warrior, saviour

    data class PRIMUS(
        val title: String = "Primus",
        val amount: Int = 2,
        val logo: Int = R.drawable.primus,
        val desc: String = "This will get us a cup of coffee while we are working on Andronix.",
        val sku_id: String = "don_starter_2"
    )

    data class BLAZE(
        val title: String = "Blaze",
        val amount: Int = 5,
        val logo: Int = R.drawable.blaze,
        val desc: String = "This will help us to pay a bit of our internet and other bills.",
        val sku_id: String = "don_cool_5"
    )

    data class WARRIOR(
        val title: String = "Warrior",
        val amount: Int = 10,
        val logo: Int = R.drawable.warrior,
        val desc: String = "This will cover some bits of our infrastructure cost that we spend on Digital Ocean, GCloud, Render etc.",
        val sku_id: String = "don_warrior_10"
    )

    data class SAVIOR(
        val title: String = "Saviour",
        val amount: Int = 2,
        val logo: Int = R.drawable.saviour,
        val desc: String = "This will go towards developing new features for Andronix and keeping things updated.",
        val sku_id: String = "don_saviour_25"
    )
}