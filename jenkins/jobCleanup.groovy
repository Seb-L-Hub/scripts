/*

Parse all Generation folders to delete generation in success older than given retention

Sample calls:

- Delete generation folders in success older than 3 months:
  generationFolderCleanUp()

- Delete generation folders in success older than 2 months:
  use(groovy.time.TimeCategory) {
    generationFolderCleanUp(2.month)
  }

- Simulate generation folders in success older than 3 months and 5 hours:
  use(groovy.time.TimeCategory) {
    generationFolderCleanUp(3.month + 5.hours,true)
  }

*/

def generationFolderCleanUp(retention=null,dryRun=false) {
  // Get currentDate
  def now = new Date(Calendar.getInstance().getTimeInMillis())
  def lastValidDate = null
  use(groovy.time.TimeCategory) {
    lastValidDate = (now-(retention ?: 6.month))
  }

  def generationFolders = generationFoldersTimestamps()

  println "Request cleanup of generation folders older than ${lastValidDate.format('YYYY-MM-dd HH:mm:ss')}"
  println ""

  def toBeCleanup = generationFolders.findAll { generationFolder,data ->
    def expired = false
    use(groovy.time.TimeCategory) {
      def buildDate = new Date(data.timestamp)
      expired = data.timestamp && buildDate < lastValidDate
      println "${expired?'*':' '} ${data.timestamp ? buildDate.format('YYYY-MM-dd HH:mm:ss') : (data.jobs ? "No jobs" : "No build success")}: ${generationFolder.fullName}"
      expired
    }
  }
  println ""

  toBeCleanup.each { generationFolder, data ->
    if(dryRun) {
    println "\t ${generationFolder.fullName}"
    } else {
      generationFolder.fullName.delete()
    }

  }
  println ""
  println "Generation folders before cleanup: ${generationFolders.size()}"
  println "Generation folders to be deleted : ${toBeCleanup.size()}"
  println "Generation folders after cleanup : ${generationFolders.size() - toBeCleanup.size()}"
}

def generationFoldersTimestamps() {
  def results = [:]

  // Parse top items to retrieve instance folders
  def instancesFolder = Jenkins.instance.items.findAll { item ->
    item instanceof com.cloudbees.hudson.plugins.folder.Folder
  }
  // Parse instancesFolder
  instancesFolder.each { instanceFolder ->
    // Parse subitems to retrieve generation folders timestamp
    instanceFolder.items.findAll { generationFolder ->
      generationFolder instanceof com.cloudbees.hudson.plugins.folder.Folder
    }.each { generationFolder ->
      if ( getExclusions().any { pattern -> generationFolder.fullName ==~ pattern } ) {
        println "Excluding folder ${generationFolder.fullName} - matches exclusion regex..."
      } else {
        def data = processGenerationFolder(generationFolder)
        results+=[(generationFolder):data]
      }
    }
  }
  return results
}


def processGenerationFolder(generationFolder) {
  def generationJobs = generationFolder.items
  def multibranch=false
  if(generationJobs && generationJobs[0] instanceof org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject) {
    generationJobs = generationJobs.size()==1 ? generationJobs[0].views.find{ view -> view.name=='tags' }?.items : []
    multibranch = generationJobs?.size()>0
  }
  def timestamp = 0
  def successJobs = generationJobs.findAll { generationJob ->
    generationJob.lastSuccessfulBuild
  }
  if(multibranch || successJobs.size()==generationJobs?.size()) {
    successJobs.each { successJob ->
      timestamp = Math.max(timestamp,getLastSuccessTimestamp(successJob)?:0)
    }
  }
  return [timestamp:timestamp, jobs:generationJobs]
}

// return
def getLastSuccessTimestamp(job) {
  def lastSuccessTimestamp = null
  build = job?.lastSuccessfulBuild
  if(build) {
    lastSuccessTimestamp = build.timestamp.getTimeInMillis()
  }
  return lastSuccessTimestamp
}

def instanceStats(items=Jenkins.instance.allItems) {
  items*.class.unique().each { type ->
    print "${type.simpleName}: "
    println items.findAll {
      type.isInstance(it)
    }.size()
  }
}

// define folders to be excluded from cleanup by regex
def getExclusions() {
  [
    /Sb\/check-hosts.*/,
    /Admin\/.*/
  ]
}

return this
