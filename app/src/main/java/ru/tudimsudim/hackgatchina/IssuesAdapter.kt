package ru.tudimsudim.hackgatchina

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import ru.tudimsudim.hackgatchina.model.Issue
import ru.tudimsudim.hackgatchina.presenter.HttpClient

interface IssueItemClick {
    fun onClick(index: Int)
}


class IssuesAdapter(val screenWidth: Int, val listener: IssueItemClick) :
    RecyclerView.Adapter<IssuesAdapter.IssueViewHolder>() {

    var issues = emptyList<Issue>()


    override fun onBindViewHolder(p0: IssueViewHolder, p1: Int) {
        p0.bind(issues[p1])
    }

    override fun getItemCount(): Int {
        return issues.count()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): IssueViewHolder {
        val inflater = LayoutInflater.from(p0.context)
        val vh = IssueViewHolder(inflater, p0)
        val verticalPadding = p0.context.resources.getDimensionPixelSize(R.dimen.issue_item_vertical_padding)
        vh.itemView.layoutParams!!.height = screenWidth - verticalPadding
        vh.itemView.setOnClickListener {
            listener.onClick(vh.adapterPosition)
        }
        return vh
    }

    fun update(issues: List<Issue>) {
        this.issues = issues
        notifyDataSetChanged()
    }

    class IssueViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.issue_list_item, parent, false)) {
        var author: TextView? = null
        var description: TextView? = null
        var issue_vote: TextView? = null
        var image: ImageView? = null

        init {
            author = itemView.findViewById(R.id.issue_author)
            description = itemView.findViewById(R.id.issue_description)
            image = itemView.findViewById(R.id.image_view)
            issue_vote = itemView.findViewById(R.id.issue_vote)
        }

        fun bind(issue: Issue) {
            author?.text = issue.author
            description?.text = issue.text
            issue_vote?.text = issue.users_like.count().toString()

            if (image != null && issue.images.count() > 0) {
                val imageUrl = HttpClient.address + "/images/" + issue.images.elementAt(0)
                Glide
                    .with(image!!.context)
                    .load(imageUrl)
                    .centerCrop()
                    .into(image!!);
            }

        }

    }

}
