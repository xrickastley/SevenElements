
class Pair<A, B> {
	public constructor(left: A, right: B) {
		this.left = left;
		this.right = right;
	}

	private left: A;
	private right: B;

	public getLeft(): A {
		return this.left;
	}

	public setLeft(left: A): void {
		this.left = left;
	}

	public getRight(): B {
		return this.right;
	}

	public setRight(right: B): void {
		this.right = right;
	}
}

export default Pair;