package tasks

import contributors.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repositories = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val allDeferred = repositories
        .map { repo ->
            async {
                log("starting loading for ${repo.name}")
                delay(3000)
                service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            }
        }
    allDeferred.awaitAll().flatten().aggregate()
}