package tasks

import contributors.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repositories = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val allDeferred = repositories
        .map { repo ->
            GlobalScope.async {
                log("starting loading for ${repo.name}")
                delay(3_000)
                service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            }
        }
    return allDeferred.awaitAll().flatten().aggregate()
}