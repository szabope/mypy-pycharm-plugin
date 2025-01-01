package works.szabope.plugins.mypy.testutil

import com.intellij.util.CatchingConsumer
import com.intellij.webcore.packaging.InstalledPackage
import com.intellij.webcore.packaging.PackageManagementService
import com.intellij.webcore.packaging.RepoPackage

class PackageManagementTestService : PackageManagementService() {

    private val installedPackages = mutableListOf<InstalledPackage>()

    override fun getAllPackages(): MutableList<RepoPackage> {
        throw NotImplementedError()
    }

    override fun reloadAllPackages(): MutableList<RepoPackage> {
        throw NotImplementedError()
    }

    override fun installPackage(
        repoPackage: RepoPackage?,
        version: String?,
        forceUpgrade: Boolean,
        extraOptions: String?,
        listener: Listener?,
        installToUser: Boolean
    ) {
        listener?.operationStarted("dummy")
        installedPackages.add(InstalledPackage(repoPackage!!.name, repoPackage.latestVersion))
        listener?.operationFinished("dummy", null)
    }

    override fun getInstalledPackagesList(): MutableList<out InstalledPackage> {
        return installedPackages
    }

    override fun uninstallPackages(installedPackages: MutableList<out InstalledPackage>?, listener: Listener?) {
        throw NotImplementedError()
    }

    override fun fetchPackageVersions(
        packageName: String?,
        consumer: CatchingConsumer<in MutableList<String>, in Exception>?
    ) {
        throw NotImplementedError()
    }

    override fun fetchPackageDetails(packageName: String?, consumer: CatchingConsumer<in String, in Exception>?) {
        throw NotImplementedError()
    }
}