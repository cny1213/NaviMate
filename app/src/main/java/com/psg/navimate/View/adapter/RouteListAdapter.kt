package com.psg.navimate.View.adapter

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.psg.navimate.databinding.RowRouteResultBinding
import com.psg.navimate.Model.Route
import com.psg.navimate.Model.Step
import com.psg.navimate.R

class RouteListAdapter(
    private val onItemClick: (route: Route, position: Int) -> Unit
) : ListAdapter<Route, RouteListAdapter.VH>(RouteDiffCallback()) {

    inner class VH(private val rowBinding: RowRouteResultBinding) : RecyclerView.ViewHolder(rowBinding.root) {
        fun bind(r: Route, pos: Int) {
            // 최적 경로 라벨
            val isOptimized = r.routeNumber.contains("-")
            val label = "* 알고리즘 적용 최적경로 *"
            val fullText = if (isOptimized) {
                "$label\n경로 ${r.routeNumber}"
            } else {
                "경로 ${r.routeNumber}"
            }

            // SpannableStringBuilder 로 부분 색상 및 크기 적용
            val ssb = SpannableStringBuilder(fullText)
            if (isOptimized) {
                val color = ContextCompat.getColor(rowBinding.root.context, R.color.colorMainBlue)
                ssb.setSpan(
                    ForegroundColorSpan(color),
                    /* start = */ 0,
                    /* end   = */ label.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                ssb.setSpan(
                    RelativeSizeSpan(0.8f),
                    0, label.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            rowBinding.textViewSummary.text = ssb


            // 2) 전체 소요시간 포맷(초 → "X시간 Y분")
            rowBinding.textViewDuration.text = formatDuration(r.duration)

            // 3) 상세 설명 조립
            val sb = StringBuilder()
            for (step in r.steps) {
                sb.append(buildStepLine(step))
                sb.append("\n")
            }
            rowBinding.textViewExplan.text = sb.toString().trimEnd()

            // 4) 클릭 리스너
            rowBinding.root.setOnClickListener { onItemClick(r, pos) }
}

        private fun formatDuration(seconds: Int): String {
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            return when {
                h > 0 && m > 0 -> "${h}시간 ${m}분"
                h > 0           -> "${h}시간"
                else            -> "${m}분"
            }
        }

        private fun buildStepLine(step: Step): String {
            // mode 한글화
            val modeStr = when (step.mode.lowercase()) {
                "walk"    -> "걷기"
                "bus"     -> "버스"
                "subway"  -> "지하철"
                else      -> step.mode
            }
            // 구간 소요시간(초 → 분)
            val durMin = step.duration / 60
            val durText = if (durMin > 0) "${durMin}분" else "${step.duration}초"

            // 장소표시: 보행 → description, 탑승 → from.name
            val place = when {
                step.mode.equals("walk", true) && !step.description.isNullOrBlank() ->
                    step.description.removePrefix("Walk to ").trim()
                step.from != null ->
                    step.from.name
                else ->
                    step.description ?: step.to?.name.orEmpty()
            }

            return "$modeStr $durText : $place"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowRouteResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), position)
    }
}