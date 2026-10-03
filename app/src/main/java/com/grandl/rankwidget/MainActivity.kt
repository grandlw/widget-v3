package com.grandl.rankwidget
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
class MainActivity:AppCompatActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main);schedule();render();findViewById<Button>(R.id.refresh).setOnClickListener{refresh()}}
 private fun schedule(){val req=PeriodicWorkRequestBuilder<RankWorker>(30,TimeUnit.MINUTES).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build();WorkManager.getInstance(this).enqueueUniquePeriodicWork("rank30m",ExistingPeriodicWorkPolicy.UPDATE,req)}
 private fun refresh(){findViewById<TextView>(R.id.status).text="OP.GG güncelleniyor…";lifecycleScope.launch{try{val data=withContext(Dispatchers.IO){RankRepository.fetch()};RankRepository.saveAndDailyDelta(this@MainActivity,data);RankWidgetProvider.updateAll(this@MainActivity);render()}catch(e:Exception){findViewById<TextView>(R.id.status).text="Güncelleme başarısız: ${e.message}"}}}
 private fun render(){val p=getSharedPreferences("rank",MODE_PRIVATE);val tier=p.getString("tier","Platinum")!!;findViewById<AuraView>(R.id.aura).setTier(tier);findViewById<TextView>(R.id.player).text="grandl#wave  •  TR";findViewById<TextView>(R.id.rank).text="$tier ${p.getString("div","1")}";findViewById<TextView>(R.id.lp).text="${p.getInt("lp",30)} LP";findViewById<TextView>(R.id.record).text="${p.getInt("wins",68)}W  •  ${p.getInt("losses",55)}L  •  %${p.getInt("wr",55)} WR";val d=p.getInt("delta",0);findViewById<TextView>(R.id.delta).text="Bugün: ${if(d>=0) "+" else ""}$d LP";findViewById<TextView>(R.id.status).text="Solo/Duo • yaklaşık 30 dakikada bir otomatik yenilenir";lifecycleScope.launch{val bmp=withContext(Dispatchers.IO){RankRepository.fetchEmblem(tier)};bmp?.let{findViewById<ImageView>(R.id.badge).setImageBitmap(it)}}}
}
