package tasks

import contributors.*
import io.reactivex.subjects.PublishSubject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

fun loadContributorsCallbacks(service: GitHubService, req: RequestData, updateResults: (List<User>) -> Unit) {
    service.getOrgReposCall(req.org).onResponse { responseRepos ->
        logRepos(req, responseRepos)
        val repos = responseRepos.bodyList()
        val subject = PublishSubject.create<List<User>>()
        for ((index, repo) in repos.withIndex()) {
            service.getRepoContributorsCall(req.org, repo.name).onResponse { responseUsers ->
                logUsers(repo, responseUsers)
                val users = responseUsers.bodyList()
                subject.onNext(users)
                if (index == repos.lastIndex) subject.onComplete()
            }
        }
        subject
            .reduce(LinkedList(), toListOfUsers())
            .subscribe { list -> updateResults(list.aggregate()) }
    }
}

private fun toListOfUsers(): (LinkedList<User>, List<User>) -> LinkedList<User> {
    return { accumulator: LinkedList<User>, currentValue: List<User> ->
        for (user in currentValue) accumulator.push(user)
        accumulator
    }
}

inline fun <T> Call<T>.onResponse(crossinline callback: (Response<T>) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            callback(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}
