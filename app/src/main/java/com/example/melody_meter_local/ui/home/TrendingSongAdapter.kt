import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.melody_meter_local.R
import com.example.melody_meter_local.model.Song

class TrendingSongAdapter (private val trendingSongList: List<Song>): RecyclerView.Adapter<TrendingSongAdapter.SongViewHolder>(){
    class SongViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val trackName: TextView = itemView.findViewById(R.id.trackName)
        val trackArtist: TextView = itemView.findViewById(R.id.trackArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val track = trendingSongList[position]
        holder.trackName.text = track.name
        holder.trackArtist.text = track.artist
    }

    override fun getItemCount(): Int {
        return trendingSongList.size
    }
}