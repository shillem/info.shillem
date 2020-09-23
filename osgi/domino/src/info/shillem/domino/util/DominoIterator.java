package info.shillem.domino.util;

import java.util.Iterator;

import lotus.domino.Base;

interface DominoIterator<T extends Base> extends AutoCloseable, Iterator<T> {

}
