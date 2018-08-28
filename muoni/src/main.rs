extern crate regex;
extern crate nalgebra as na;
extern crate num_bigint as ni;
extern crate num_complex as nc;
extern crate num_traits;

#[macro_use]
extern crate lazy_static;

use std::env;
use std::fs::File;
use std::io::prelude::*;

mod ast;
mod lexer;
mod parser;
mod interpreter;

use interpreter::value::*;
use interpreter::value::unit::*;

fn main() {
    let args: Vec<String> = env::args().collect();
    let filename = args.get(1)
        .expect("Please give a filename.");
    let mut file = File::open(filename)
        .expect("File does not exist");
    let mut code = String::new();
    file.read_to_string(&mut code)
        .expect("error reading file");

    let lexemes = lexer::lex(code);

    let controls = parser::parse(lexemes);

    let test = V::from(&ast::RValue::Bool(true));
    let test2 = V::new(String::from("asdf"));
    println!("{:?}",test2.add(&test));
    let test3 = test2.add(&test);
    println!("{:?}",V::new(true).sub(&V::new(5)));

    let test = V::new(UV::from(5));
    println!("{:?}",test);
    println!("{:?}",V::new(V::new(true).sub(&V::new(5)).unwrap().to_uci()));
}
