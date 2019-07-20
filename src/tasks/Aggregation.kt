package tasks

import contributors.User
import java.util.stream.Collectors

/*
TODO: Write aggregation code.

 In the initial list each user is present several times, once for each
 repository he or she contributed to.
 Merge duplications: each user should be present only once in the resulting list
 with the total value of contributions for all the repositories.
 Users should be sorted in a descending order by their contributions.

 The corresponding test can be found in test/tasks/AggregationKtTest.kt.
 You can use 'Navigate | Test' menu action (note the shortcut) to navigate to the test.
*/
fun List<User>.aggregate(): List<User> {

    val streams =
        if (this.size < 100_000) this.groupBy { it.login }.entries.stream()
        else this.groupBy { it.login }.entries.parallelStream()

    return streams
        .map { groupOfUser -> User(groupOfUser.key, groupOfUser.value.sumBy { it.contributions }) }
        .sorted { o1, o2 -> o2.contributions - o1.contributions }
        .collect(Collectors.toList())
}
