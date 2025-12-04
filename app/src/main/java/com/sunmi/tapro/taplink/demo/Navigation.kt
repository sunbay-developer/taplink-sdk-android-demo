package com.sunmi.tapro.taplink.demo

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sunmi.tapro.taplink.demo.pages.AuthTransactionPage
import com.sunmi.tapro.taplink.demo.pages.BatchCloseTransactionPage
import com.sunmi.tapro.taplink.demo.pages.ForcedAuthTransactionPage
import com.sunmi.tapro.taplink.demo.pages.MainMenuPage
import com.sunmi.tapro.taplink.demo.pages.PostAuthTransactionPage
import com.sunmi.tapro.taplink.demo.pages.QueryTransactionPage
import com.sunmi.tapro.taplink.demo.pages.RefundTransactionPage
import com.sunmi.tapro.taplink.demo.pages.SaleTransactionPage
import com.sunmi.tapro.taplink.demo.pages.TipAdjustPage
import com.sunmi.tapro.taplink.demo.pages.VoidTransactionPage

/**
 * 导航路由常量
 */
const val PAGE_MAIN_MENU = "page_main_menu"
const val PAGE_SALE = "page_sale"
const val PAGE_AUTH = "page_auth"
const val PAGE_VOID = "page_void"
const val PAGE_REFUND = "page_refund"
const val PAGE_TIP_ADJUST = "page_tip_adjust"
const val PAGE_INCREMENTAL_AUTH = "page_incremental_auth"
const val PAGE_POST_AUTH = "page_post_auth"
const val PAGE_FORCED_AUTH = "page_forced_auth"
const val PAGE_BATCH_CLOSE = "page_batch_close"
const val PAGE_QUERY = "page_query"

/**
 * Taplink Demo 导航配置
 */
@Composable
fun TaplinkDemoNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = PAGE_MAIN_MENU) {
        initNavBuilder(navController, builder = this)
    }
}

/**
 * 初始化导航构建器
 */
fun initNavBuilder(navController: NavHostController, builder: NavGraphBuilder) {
    builder.apply {
        // 主菜单页面
        composable(PAGE_MAIN_MENU) {
            MainMenuPage(navController = navController)
        }

        // 销售交易页面
        composable(PAGE_SALE) {
            SaleTransactionPage(navController = navController)
        }

        // 预授权页面
        composable(PAGE_AUTH) {
            AuthTransactionPage(navController = navController)
        }

        // 撤销交易页面
        composable(PAGE_VOID) {
            VoidTransactionPage(navController = navController)
        }

        // 退款交易页面
        composable(PAGE_REFUND) {
            RefundTransactionPage(navController = navController)
        }

        // 小费调整页面
        composable(PAGE_TIP_ADJUST) {
            TipAdjustPage(navController = navController)
        }

        // 预授权完成页面
        composable(PAGE_POST_AUTH) {
            PostAuthTransactionPage(navController = navController)
        }

        // 强制授权页面
        composable(PAGE_FORCED_AUTH) {
            ForcedAuthTransactionPage(navController = navController)
        }

        // 批次关闭页面
        composable(PAGE_BATCH_CLOSE) {
            BatchCloseTransactionPage(navController = navController)
        }

        // 查询交易页面
        composable(PAGE_QUERY) {
            QueryTransactionPage(navController = navController)
        }
    }
}

