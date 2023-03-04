package amazonreviewpersistance

import fs2.io.file.Path

object ReviewFile {

  lazy val inputValue = sys.props.get("filepath")

}
