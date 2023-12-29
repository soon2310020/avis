package saleson;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LombokTest {

	@Test
	public void test() {
		A1 a1 = new A1("x");
		log.info("(a=x) with @Getter @Setter");
		log.info("toString: " + a1.toString());
		log.info("equals(a=x): " + a1.equals(new A1("x")));
		log.info("equals(a=z): " + a1.equals(new A1("z")));
		System.out.println();

		A11 a11 = new A11("x", "y");
		log.info("(a=x, b=y) with @Data extends @Getter @Setter");
		log.info("toString: " + a11);
		log.info("equals(a=x, b=y): " + a11.equals(new A11("x", "y")));
		log.info("equals(a=z, b=y): " + a11.equals(new A11("z", "y")));
		log.info("equals(a=x, b=z): " + a11.equals(new A11("x", "z")));
		System.out.println();

		A12 a12 = new A12("x", "y");
		log.info("(a=x, b=y) with @Getter @Setter extends @Getter @Setter");
		log.info("toString: " + a12);
		log.info("equals(a=x, b=y): " + a12.equals(new A12("x", "y")));
		log.info("equals(a=z, b=y): " + a12.equals(new A12("z", "y")));
		log.info("equals(a=x, b=z): " + a12.equals(new A12("x", "z")));
		System.out.println();

		A13 a13 = new A13("x", "y");
		log.info("(a=x, b=y) with @Getter @Setter @ToString(callSuper=false) @EqualsAndHashCode(callSuper=true) extends @Getter @Setter");
		log.info("toString: " + a13);
		log.info("equals(a=x, b=y): " + a13.equals(new A13("x", "y")));
		log.info("equals(a=z, b=y): " + a13.equals(new A13("z", "y")));
		log.info("equals(a=x, b=z): " + a13.equals(new A13("x", "z")));
		System.out.println();

		A14 a14 = new A14("x", "y");
		log.info("(a=x, b=y) with @Getter @Setter @ToString(callSuper=true) @EqualsAndHashCode(callSuper=true) extends @Getter @Setter");
		log.info("toString: " + a14);
		log.info("equals(a=x, b=y): " + a14.equals(new A14("x", "y")));
		log.info("equals(a=z, b=y): " + a14.equals(new A14("z", "y")));
		log.info("equals(a=x, b=z): " + a14.equals(new A14("x", "z")));
		System.out.println();

		B1 b1 = new B1("x");
		log.info("(a=x) @Data");
		log.info("toString: " + b1);
		log.info("equals(a=x): " + b1.equals(new B1("x")));
		log.info("equals(a=z): " + b1.equals(new B1("z")));
		System.out.println();

		B11 b11 = new B11("x", "y");
		log.info("(a=x, b=y) with @Data extends @Data");
		log.info("toString: " + b11);
		log.info("equals(a=x, b=y): " + b11.equals(new B11("x", "y")));
		log.info("equals(a=z, b=y): " + b11.equals(new B11("z", "y")));
		log.info("equals(a=x, b=z): " + b11.equals(new B11("x", "z")));
		System.out.println();

		B12 b12 = new B12("x", "y");
		log.info("(a=x, b=y) with @Getter @Setter extends @Data");
		log.info("toString: " + b12);
		log.info("equals(a=x, b=y): " + b12.equals(new B12("x", "y")));
		log.info("equals(a=z, b=y): " + b12.equals(new B12("z", "y")));
		log.info("equals(a=x, b=z): " + b12.equals(new B12("x", "z")));
		System.out.println();

		B13 b13 = new B13("x", "y");
		log.info("(a=x, b=y) with @Getter @Setter @ToString(callSuper=false) @EqualsAndHashCode(callSuper=true) extends @Data");
		log.info("toString: " + b13);
		log.info("equals(a=x, b=y): " + b13.equals(new B13("x", "y")));
		log.info("equals(a=z, b=y): " + b13.equals(new B13("z", "y")));
		log.info("equals(a=x, b=z): " + b13.equals(new B13("x", "z")));
		System.out.println();

		B14 b14 = new B14("x", "y");
		log.info("(a=x, b=y) with @Getter @Setter @ToString(callSuper=true) @EqualsAndHashCode(callSuper=true) extends @Data");
		log.info("toString: " + b14);
		log.info("equals(a=x, b=y): " + b14.equals(new B14("x", "y")));
		log.info("equals(a=z, b=y): " + b14.equals(new B14("z", "y")));
		log.info("equals(a=x, b=z): " + b14.equals(new B14("x", "z")));
		System.out.println();
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class A1 {
		private String a;
	}

	@Data
	@NoArgsConstructor
	public static class A11 extends A1 {
		private String b;

		public A11(String a, String b) {
			super(a);
			this.b = b;
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class A12 extends A1 {
		private String b;

		public A12(String a, String b) {
			super(a);
			this.b = b;
		}
	}

	@Getter
	@Setter
	@ToString(callSuper = false)
	@EqualsAndHashCode(callSuper = false)
	@NoArgsConstructor
	public static class A13 extends A1 {
		private String b;

		public A13(String a, String b) {
			super(a);
			this.b = b;
		}
	}

	@Getter
	@Setter
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	public static class A14 extends A1 {
		private String b;

		public A14(String a, String b) {
			super(a);
			this.b = b;
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class B1 {
		private String a;
	}

	@Data
	@NoArgsConstructor
	public static class B11 extends B1 {
		private String b;

		public B11(String a, String b) {
			super(a);
			this.b = b;
		}
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class B12 extends B1 {
		private String b;

		public B12(String a, String b) {
			super(a);
			this.b = b;
		}
	}

	@Getter
	@Setter
	@ToString(callSuper = false)
	@EqualsAndHashCode(callSuper = false)
	@NoArgsConstructor
	public static class B13 extends B1 {
		private String b;

		public B13(String a, String b) {
			super(a);
			this.b = b;
		}
	}

	@Getter
	@Setter
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	public static class B14 extends B1 {
		private String b;

		public B14(String a, String b) {
			super(a);
			this.b = b;
		}
	}

}
