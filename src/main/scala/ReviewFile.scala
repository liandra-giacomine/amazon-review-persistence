package amazonreviewpersistance

object ReviewFile {

  lazy val inputValue = sys.props.get("filepath")

  lazy val path = java.nio.file.Paths.get(inputValue.get)

  lazy val size = java.nio.file.Files.size(path)
}
