package com.psg.navimate.View.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.psg.navimate.Model.Step
import com.psg.navimate.R
import com.psg.navimate.databinding.RowStepItemBinding

class StepListAdapter(
    private val originName: String,
    private val originLat: Double,
    private val originLng: Double,
    private val steps: List<Step>,
    private val destName: String,
    private val destLat: Double,
    private val destLng: Double,
    private val onItemClick: (lat: Double, lng: Double) -> Unit
) : RecyclerView.Adapter<StepListAdapter.VH>() {

    inner class VH(private val b: RowStepItemBinding) : RecyclerView.ViewHolder(b.root) {
        fun bindHeader(title: String, lat: Double, lng: Double) {
            // 출발/도착 아이콘
            b.imageBmw.setImageResource(R.drawable.icon_walk)
            b.textViewStepHeader.text = title
            b.textViewStepDesc.text = ""        // 헤더일 땐 description 은 비워두고
            b.root.setOnClickListener {
                Log.d("StepClick", "Header 클릭: $title → lat=$lat, lng=$lng")
                onItemClick(lat, lng)
            }
        }

        /**
         * @param stepIndex : steps 리스트에서의 인덱스 (0 부터)
         */
        fun bindStep(step: Step, stepIndex: Int) {
            // 1) mode 한글화
            val (modeStr, iconRes) = when (step.mode.lowercase()) {
                "walk"   -> "걷기"    to R.drawable.icon_walk
                "bus"    -> "버스"    to R.drawable.icon_bus
                "subway" -> "지하철"  to R.drawable.icon_subway
                else     -> step.mode to R.drawable.icon_error
            }
            // 2) duration 초 → 분
            val mins = kotlin.math.ceil(step.duration / 60.0).toInt()
            val durStr = "${mins}분"

            // 3) place 추출 (walk → description, else → from.name or to.name)
            val placeStr = when {
                step.mode.equals("walk", true) && !step.description.isNullOrBlank() ->
                    step.description.removePrefix("Walk to ").trim()
                step.from != null -> step.from.name
                step.to != null -> step.to.name
                else -> step.description.orEmpty()
            }

            // 4) line 정보 처리
            if (!step.line.isNullOrBlank()){
                b.textViewLine.apply{
                    text = step.line
                    visibility = View.VISIBLE
                }
            } else{
                b.textViewLine.visibility = View.GONE
            }

            // 바인딩
            b.imageBmw.setImageResource(iconRes)
            b.textViewStepHeader.text = "$modeStr · $durStr"
            b.textViewStepDesc.text   = placeStr

            // 클릭 시: from 있으면 from.coord, else to.coord
            b.root.setOnClickListener {
                // 클릭 시 실제 사용할 coord 계산
                val rawCoord = when {
                    step.mode.equals("walk", true) -> {
                        // 다음 스텝 index = stepIndex+1
                        steps.getOrNull(stepIndex + 1)?.from?.coord
                    }
                    else -> {
                        // Bus/Subway는 from.coord(승차위치) 대신 to.coord(하차)로 옮기고 싶으면 여기 수정
                        //step.from?.coord ?: step.to?.coord
                        step.to?.coord
                    }
                }

                Log.d("StepClick", ">>> mode=${step.mode}, idx=$stepIndex, rawCoord=$rawCoord")

                rawCoord?.let { coord ->
                    val lat = coord[0]
                    val lng = coord[1]
                    Log.d("StepClick", "    -> onItemClick(lat=$lat, lng=$lng)")
                    onItemClick(lat, lng)
                } ?: run {
                    Log.w("StepClick", "    rawCoord null → 클릭 동작 없음")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding  = RowStepItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    // 총 항목 수는 스텝 수 + 출발지 + 도착지
    override fun getItemCount(): Int = steps.size

//    override fun onBindViewHolder(holder: VH, position: Int) {
////        when (position) {
////            0 -> {
////                // 출발지
////                holder.bindHeader("출발지: $originName", originLat, originLng)
////            }
////            itemCount - 1 -> {
////                // 도착지
////                holder.bindHeader("도착지: $destName", destLat, destLng)
////            }
////            else -> {
////                // 스텝
////                val stepIndex = position - 1
////                holder.bindStep(steps[stepIndex], stepIndex)
////            }
////        }
//
//        // 스텝
//        val stepIndex = position - 1
//        holder.bindStep(steps[stepIndex], stepIndex)
//    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bindStep(steps[position], position)
    }
}