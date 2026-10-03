package com.grandl.rankwidget
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
class RankWorker(c:Context,p:WorkerParameters):CoroutineWorker(c,p){override suspend fun doWork():Result=try{val d=RankRepository.fetch();RankRepository.saveAndDailyDelta(applicationContext,d);RankWidgetProvider.updateAll(applicationContext);Result.success()}catch(e:Exception){Result.retry()}}
