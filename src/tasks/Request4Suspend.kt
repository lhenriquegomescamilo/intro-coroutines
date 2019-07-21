package tasks

import contributors.*

suspend fun loadContributorsSuspend(req: RequestData): List<User> {
    val (username, password, org) = req
    val service = createGitHubService(username, password)

    val repositories = service.getOrgRepos(org)
        .also { logRepos(req, it) }
        .bodyList()


    return repositories
        .flatMap { repository ->
            service.getRepoContributors(org, repository.name)
                .also { logUsers(repository, it) }
                .bodyList()
        }.aggregate()

}