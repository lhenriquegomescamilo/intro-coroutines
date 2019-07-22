package tasks

import contributors.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val channel = Channel<List<User>>()
        val repos = service.getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .bodyList()
        val allUsers = LinkedList<User>()
        for (repo in repos) {
            launch {
                val users = service
                    .getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }
        repeat(repos.size) {
            val users = channel.receive()
            for (user in users) allUsers.push(user)
            updateResults(allUsers.aggregate(), it == repos.lastIndex)
    }
}
}