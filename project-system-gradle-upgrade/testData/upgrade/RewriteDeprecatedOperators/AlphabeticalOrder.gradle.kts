android {
  buildToolsVersion("1.2.3")
  compileSdkVersion(30)
  flavorDimensions("paid", "country")
  defaultConfig {
    maxSdkVersion(30)
    minSdkVersion(28)
    targetSdkVersion(29)
    setTestFunctionalTest(true)
    setTestHandleProfiling(false)
  }
  productFlavors {
    create("foo") {
      setDimension("paid")
      maxSdkVersion(29)
      minSdkVersion(27)
      targetSdkVersion(28)
      setTestFunctionalTest(false)
      setTestHandleProfiling(true)
    }
  }
}