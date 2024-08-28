
import unittest
import deploy as Deploy
import re
import os
from deploy import FileHelper
import sys
import subprocess

class CommandLineHelperTest(unittest.TestCase):
    cliHelper = Deploy.CommandLineHelper()

    def test_runCommand_lsCommand_ericssonTestDirectory(self):
        expected = 0
        command = 'ls'
        actual = self.cliHelper.runCommand(command)
        self.assertEquals(expected, actual)

    def test_runSubprocess(self):
        expected = 0
        command = 'ls'
        actual = self.cliHelper.runSubprocess(command)

        self.assertEquals(expected, actual)

class FileHelperTest(unittest.TestCase):
    fileHelper = Deploy.FileHelper()

    def test_readFileToString(self):
        expected = 'this is for testing'

        actual = self.fileHelper.readFileToString('test/test.txt')
        self.assertEquals(expected, actual)

    def test_checkDirectoryExists_True(self):
        actual = self.fileHelper.checkDirectoryExists('test')
        self.assertTrue(actual)

    def test_checkDirectoryExists_False(self):
        actual = self.fileHelper.checkDirectoryExists('invalidDirectory')
        self.assertFalse(actual)

    def test_checkFileExists_True(self):
        actual = self.fileHelper.checkFileExists('deploy.py')
        self.assertTrue(actual)

    def test_checkFileExists_False(self):
        actual = self.fileHelper.checkFileExists('invalidFile')
        self.assertTrue(actual)

    def test_unzipFile(self):
        expected = 'test1.txt'

        os.chdir('test')
        self.fileHelper.unzipFile('test.zip')

        dirList = os.listdir('.')
        fileFound = False
        for file in dirList:
            if file == expected:
                fileFound = True
        try:
            os.remove(expected)
            os.chdir('..')
        except OSError:
            pass

        self.assertTrue(fileFound)

    def test_getFullFileName(self):
        expected = 'install.properties'
        fileNamePart = 'install'
        directory = '.'

        actual = self.fileHelper.getFullFileName(directory, fileNamePart)

        self.assertEquals(expected, actual)

class UrlHelperTest(unittest.TestCase):
    urlHelper = Deploy.UrlHelper()

    def test_downloadFileFromUrl(self):
        expected = 'ERICenmsgfmx_CXP9031866-1.7.1.rpm'
        url = 'https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/oss/servicegroupcontainers/ERICenmsgfmx_CXP9031866/1.7.1/ERICenmsgfmx_CXP9031866-1.7.1.rpm'
        self.urlHelper.downloadFileFromUrl(url)


        dirList = os.listdir('.')
        fileFound = False
        for file in dirList:
            if file == expected:
                fileFound = True

        try:
            os.remove(expected)
        except OSError:
            pass

        self.assertTrue(fileFound)

    def test_getResponse(self):
        url = 'https://cifwk-oss.lmera.ericsson.se/getMasterSedVersion/'
        pattern = '\d*\.\d*\.\d*' #e.g. 16.5.9
        actual = self.urlHelper.getResponse(url)

        self.assertTrue(actual)
        self.assertTrue(re.match(pattern, actual))

class RepositoryTest(unittest.TestCase):
    repository = Deploy.Repository()
    def test_getGitTagCommand(self):

        expected = 'git checkout tags/defaultConfigurableEntities-1.15.4'

        repository = 'defaultConfigurableEntities'
        version = '1.15.4'
        actual = self.repository._getGitTagCommand(repository, version)

        self.assertEquals(expected, actual)

class ContinousIntegrationRestHelperTest(unittest.TestCase):
    ciRestHelper = Deploy.ContinousIntegrationRestHelper()
    _enmVersion = '16.5'
    def test_getLatestEnmProductNumber(self):

        pattern = '\d*\.\d*\.\d*' #e.g. 16.5.9
        actual = self.ciRestHelper.getLatestEnmProductNumber(self._enmVersion)

        self.assertTrue(actual)
        self.assertTrue(re.match(pattern, actual))

    def test_getMasterSedVersion(self):
        pattern = '\d*\.\d*\.\d*' #e.g. 1.0.108
        actual = self.ciRestHelper.getMasterSedVersion()

        self.assertTrue(actual)
        self.assertTrue(re.match(pattern, actual))

    def test_getAOMNumber(self):
        pattern = 'AOM\d*' #e.g. AOM901151
        actual = self.ciRestHelper.getAomNumber(self._enmVersion)

        self.assertTrue(actual)
        self.assertTrue(re.match(pattern, actual))

    def test_getRState(self):
        pattern = '[A-Z]\d[A-Z]' #e.g. R1Y
        actual = self.ciRestHelper.getRState(self._enmVersion)

        self.assertTrue(actual)
        self.assertTrue(re.match(pattern, actual))

    def test_getEnmDropVersion(self):
        pattern = '\d*\.\d*\.\d*' #e.g. 1.20.21
        actual = self.ciRestHelper.getEnmDropVersion(self._enmVersion)

        self.assertTrue(actual)
        self.assertTrue(re.match(pattern, actual))

    def test_getLitpDropVersion(self):
        pattern = '\d*\.\d*\.\d*' #e.g. 2.37.25
        actual = self.ciRestHelper.getLitpDropVersion(self._enmVersion)

        self.assertTrue(actual)
        self.assertTrue(re.match(pattern, actual))

    def test_getIsoContents(self):
        actual = self.ciRestHelper.getIsoContents(self._enmVersion)

        self.assertTrue(actual)

    def test_generateFeatureDownloadLink(self):
        expected = 'https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/oss/itpf/deployment/descriptions/ERICenmdeploymenttemplates_CXP9031758/1.16.9/ERICenmdeploymenttemplates_CXP9031758-1.16.9.rpm'

        feature = 'ERICenmdeploymenttemplates_CXP9031758'
        version = '1.16.9'
        location = 'itpf/deployment/descriptions'
        fileType = 'rpm'

        actual = self.ciRestHelper.generateFeatureDownloadLink(feature, version, location, fileType)

        self.assertEqual(expected, actual)

class DeploymentSupportToolingHelperTest(unittest.TestCase):
    dstHelper = Deploy.DeploymentSupportToolingHelper()
    _isoContents = '''{
"deliveryDrop":"16.5",
"group":"com.ericsson.oss.presentation.server.cm",
"mediaCategory":"testware",
"mediaPath":"None",
"name":"ERICTAFbulkrestapi_CXP9032300",
"number":"CXP9032300",
"platform":"None",
"type":"jar",
"url":"https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/oss/presentation/server/cm/ERICTAFbulkrestapi_CXP9032300/1.4.6/ERICTAFbulkrestapi_CXP9032300-1.4.6.jar",
"version":"1.4.6"
},
{
"deliveryDrop":"16.5",
"group":"com.ericsson.oss.itpf.deployment.descriptions",
"mediaCategory":"ms",
"mediaPath":"None",
"name":"ERICenmdeploymenttemplates_CXP9031758",
"number":"CXP9031758",
"platform":"None",
"type":"rpm",
"url":"https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/oss/itpf/deployment/descriptions/ERICenmdeploymenttemplates_CXP9031758/1.16.9/ERICenmdeploymenttemplates_CXP9031758-1.16.9.rpm",
"version":"1.16.9"
},'''

    bomFileContents = '''[Deployment Support Tooling Versions]
dst-version 1.15.4
dce-version 1.16.9

[Deployment Name, Group, Artifact, Version]
deployment-name=6svc_enm_physical_test
deployment-GAV=com.ericsson.oss.itpf.deployment.descriptions:enm-full-cdb-deployment:1.16.9'''

    def test_getFeatureVersion(self):
        expected = '1.16.9'
        feature = 'ERICenmdeploymenttemplates_CXP9031758'

        actual = self.dstHelper._getFeatureVersion(self._isoContents, feature)

        self.assertEqual(expected, actual)

    def test_getFileWithDstVersions(self):
        expected = self.bomFileContents

        actual = self.dstHelper.getBomFileWithDstVersions('ericssontest/deploymentDescriptions/')

        self.assertEquals(expected, actual)

    def test_getDeploymentServiceToolVersion(self):
        expected = '1.15.4'
        pattern = '\d*\.\d*\.\d*'

        actual = self.dstHelper.getDeploymentSupportToolingVersion(self.bomFileContents)

        self.assertEquals(expected, actual)
        self.assertTrue(re.match(pattern, actual))

    def test_getDefaultConfigurableEntitiesVersion(self):
        expected = '1.16.9'
        pattern = '\d*\.\d*\.\d*'

        actual = self.dstHelper.getDefaultConfigurableEntitiesVersion(self.bomFileContents)

        self.assertEquals(expected, actual)
        self.assertTrue(re.match(pattern, actual))

class InstallHelperTest(unittest.TestCase):
    enmVersion = '16.5'
    installHelper = Deploy.InstallHelper(enmVersion)

    def test_generateDeploymentPackageList_1(self):
        expected = 'ERICesnstreamTerminator_CXP1234567::Latest@@ERICesnproduct_CXP1234567::Latest@@ERICesnproductx_CSP1234567::Latest'

        deploymentPackageList = ['ERICesnstreamTerminator_CXP1234567::Latest', 'ERICesnproduct_CXP1234567::Latest', 'ERICesnproductx_CSP1234567::Latest']

        actual = self.installHelper._generateDeploymentPackageList(deploymentPackageList)

        self.assertEquals(expected, actual)

    def test_generateDeploymentPackageList_2(self):
        expected = 'ERICesnstreamTerminator_CXP1234567::1.2.34@@ERICesnproduct_CXP1234567::Latest@@ERICesnproductx_CSP1234567::Latest'

        deploymentPackageList = ['ERICesnstreamTerminator_CXP1234567::1.2.34', 'ERICesnproduct_CXP1234567::Latest', 'ERICesnproductx_CSP1234567::Latest']
        actual = self.installHelper._generateDeploymentPackageList(deploymentPackageList)

        self.assertEquals(expected, actual)

    def test_generateDeploymentPackageList_3(self):
        expected = 'ERICenmsgesnstreamterminatorsg_CXP111111::https://cifwk-oss.lmera.ericsson.se/static/tmpUploadSnapshot//2016-03-09_10-45-16/ERICenmsgesnstreamterminatorsg_CXP111111-1.0.1-SNAPSHOT.rpm@@ERICesnproduct_CXP1234567::Latest@@ERICesnproductx_CSP1234567::Latest'

        deploymentPackageList = ['ERICenmsgesnstreamterminatorsg_CXP111111::https://cifwk-oss.lmera.ericsson.se/static/tmpUploadSnapshot//2016-03-09_10-45-16/ERICenmsgesnstreamterminatorsg_CXP111111-1.0.1-SNAPSHOT.rpm', 'ERICesnproduct_CXP1234567::Latest', 'ERICesnproductx_CSP1234567::Latest']

        actual = self.installHelper._generateDeploymentPackageList(deploymentPackageList)

        self.assertEquals(expected, actual)



if __name__ == "__main__":
    unittest.main()
