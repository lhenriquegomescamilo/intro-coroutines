package tasks

import contributors.*
import java.util.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service.getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()
    val allUsers = LinkedList<User>()
    for ((index, repo) in repos.withIndex()) {
        val users = service
            .getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()

        for (user in users) allUsers.push(user)
        updateResults(allUsers.aggregate(), index == repos.lastIndex)
    }
}
