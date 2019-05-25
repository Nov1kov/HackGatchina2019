package ru.tudimsudim.hackgatchina

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.tudimsudim.hackgatchina.model.Issue

class IssuesAdapter : RecyclerView.Adapter<IssuesAdapter.IssueViewHolder>() {

    var issues = emptyList<Issue>()

    override fun onBindViewHolder(p0: IssueViewHolder, p1: Int) {
        p0.bind(issues[p1])
    }

    override fun getItemCount(): Int {
        return issues.count()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): IssueViewHolder {
        val inflater = LayoutInflater.from(p0.context)
        return IssueViewHolder(inflater, p0)
    }

    fun update(issues: List<Issue>) {
        this.issues = issues
        notifyDataSetChanged()
    }

    class IssueViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.issue_list_item, parent, false)) {
            private var author: TextView? = null
            private var description: TextView? = null
            private var image: ImageView? = null

            init {
                author = itemView.findViewById(R.id.issue_author)
                description = itemView.findViewById(R.id.issue_description)
                image = itemView.findViewById(R.id.image_view)
            }

            fun bind(movie: Issue) {
                author?.text = movie.author
                description?.text = movie.text
            }

    }

}
