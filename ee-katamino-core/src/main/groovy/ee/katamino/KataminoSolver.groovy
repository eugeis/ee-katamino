/*
 * Copyright 2015-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ee.katamino

import java.util.concurrent.ArrayBlockingQueue

'''
FIELD

5
4
3
2
1
0 1 2 3 4 5 6 7 8

e.g.

L =>

5
4 X
3 X
2 X
1 X X
0 1 2 3 4 5 6 7 8

'''
/**
 * @author Eugen Eisler
 */
class Cell implements Cloneable, Serializable {
    final static String EMPTY = '#'
    int h, v
    String value = EMPTY

    boolean isEmpty() {
        value == EMPTY
    }

    void clear() {
        value = EMPTY
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Cell that = (Cell) o

        if (h != that.h) return false
        if (v != that.v) return false

        return true
    }

    int hashCode() {
        int result
        result = h
        result = 31 * result + v
        return result
    }

    static Cell parse(int pos, String value) {
        String posStr = String.valueOf(pos)
        new Cell(h: Integer.valueOf(posStr.substring(0,1)), v: Integer.valueOf(posStr.substring(1,2)), value: value)
    }

    @Override
    public String toString() {
        return "Cell{" +
                "h=" + h +
                ", v=" + v +
                '}';
    }
}

class Mid {
    Cell min, max
    int size, ov, mid, diff
}

class Field {
    int height = 5
    int width
    Cell[][] space
    def draw

    void setWidth(int width) {
        this.width = width
        init()
    }

    void setHeight(int height) {
        this.height = height
        init()
    }

    void init() {
        space = new String[width + 1][height + 1]
        (1..height).each { int v ->
            (1..width).each { int h ->
                space[h][v] = new Cell(h: h, v: v)
            }
        }
    }

    void place(Figure figure) {
        figure.cells.each { Cell cell ->
            Cell pos = space[cell.h][cell.v]
            if(pos.empty) {
                pos.value = figure.name
            } else {
                println "Place: Cell has '$pos.value' on $cell, shall be null to set $figure.name"
                throw new RuntimeException("Cell has '$pos.value' on $cell, shall be null to set $figure.name")
            }
        }
    }

    void take(Figure figure) {
        figure.cells.each { Cell cell ->
            Cell pos = space[cell.h][cell.v]
            if(figure.name.equals(pos.value)) {
                pos.clear()
            } else if(!pos.empty) {
                println "Take: Wrong value '$pos.value' on $cell, shall be $figure.name"
                throw new RuntimeException("Wrong value '$pos.value' on $cell, shall be $figure.name")
            }
        }
    }

    boolean fits(Figure figure) {
        boolean ret = figure.isValidPosition(width, height)
        if(ret) {
            def notEmpty = figure.cells.find { Cell cell ->
                !space[cell.h][cell.v].empty
            }
            ret = ret & !notEmpty
        }
        ret
    }

    List<Cell> freeLeftCells() {
        def ret = []
        for(int h=1; h <= width;h++) {
            for(int v=1;v<=height;v++) {
                Cell pos = space[h][v]
                if(pos.empty) {
                    ret << pos
                }
            }
            if(ret) {
                break
            }
        }
        ret
    }

    void draw() {
        if(draw) {
            draw(this)
        } else {
            println '->'
            Cell pos
            (height..1).each { int v ->
                (1..width).each { int h ->
                    print space[h][v].value
                }
                println ''
            }
            println '<-'
        }
    }

}

class Figure {
    String name
    double[] color
    boolean symmetric = false
    private boolean inverted = false
    //0, 90, 180, 270�
    private int orientation = 0
    List<Cell> cells, orgCells
    Mid h = new Mid()
    Mid v = new Mid()
    def draw

    void setCells(def flatCells) {

        orgCells = flatCells.collect { Cell.parse(it, name) }
        cells = orgCells.clone()
        changeCells(cells)
    }

    void changeCells(List<Cell> cells) {
        h.min = cells.min { Cell cell -> cell.h }
        h.max = cells.max { Cell cell -> cell.h }
        h.size = h.max.h - h.min.h + 1
        h.ov = h.size % 2
        h.mid = (h.size - h.ov) / 2
        h.diff = (h.mid + h.ov)

        v.min = cells.min { Cell cell -> cell.v }
        v.max = cells.max { Cell cell -> cell.v }
        v.size = v.max.v - v.min.v + 1
        v.ov = v.size % 2
        v.mid = (v.size - v.ov) / 2
        v.diff = (v.mid + v.ov)

        if(cells.size() != orgCells.size()) {
            throw new RuntimeException("The size of cells is different current '${cells.size()} != ${orgCells.size()}")
        } else {
            this.cells = cells
        }
    }

    void invert() {
        resetPosition()

        inverted = !inverted

        cells.each { Cell pos ->
            Cell newPos
            if(pos.h > h.diff) {
                pos.h -= h.diff
            } else if (pos.h < h.diff || h.ov == 0) {
                pos.h += h.diff
            }
        }
        changeCells cells
    }

    void turn() {
        resetPosition()

        if(orientation == 270) {
            orientation = 0
        } else {
            orientation += 90
        }

        cells.each { Cell cell ->
            int v = h.size - cell.h + 1
            cell.h = cell.v
            cell.v = v
        }
        changeCells cells
    }

    void resetPosition() {
        int h = h.min.h - 1
        int v = v.min.v - 1
        move(h, v)
    }

    void move(int right, int top) {
        cells.each { Cell cell ->
            cell.h += right
            cell.v += top
        }
        changeCells cells
    }

    boolean hasNextPosition() {
        return orientation < 270 || (!symmetric && !inverted)
    }

    void nextPosition() {
        if(orientation < 270) {
            turn()
        } else if(!symmetric && !inverted) {
            turn()
            invert()
        }
    }

    void reset() {
        inverted = false
        orientation = 0
        changeCells orgCells.clone()
    }

    void draw(boolean detailed = false) {
        if(draw) {
            draw(this)
        } else {
            if (detailed) {
                println "\n$name(orientation=$orientation, inverted=$inverted):"
            }
            (5..1).each { int v ->
                (1..5).each { int h ->
                    print cells.find { Cell cell -> cell.h == h && cell.v == v } ? name : '#'
                }
                println ''
            }
        }
    }

    boolean isValidPosition(int maxH, int maxV) {
        boolean ret = !cells.find { Cell cell -> cell.h <=0 || cell.v <= 0 || cell.h > maxH || cell.v > maxV }
        ret
    }

    String toString() {
        "$name(orientation=$orientation, inverted=$inverted, smetric=$symmetric)"
    }
}


class SmallSlam {
    def allFigures = [
            'L': new Figure(name: 'L', cells: [11, 12, 13, 14, 21], color: [1.0f, 0.0f, 0.0f]),
            '1': new Figure(name: '1', cells: [21, 22, 23, 24, 13], color: [0.0f, 1.0f, 0.0f]),
            'T': new Figure(name: 'T', cells: [13, 21, 22, 23, 33], color: [0.0f, 0.0f, 1.0f], symmetric: true),
            'P': new Figure(name: 'P', cells: [11, 12, 13, 22, 23], color: [1.0f, 1.0f, 0.0f]),
            'W': new Figure(name: 'W', cells: [11, 21, 22, 32, 33], color: [1.0f, 0.0f, 1.0f], symmetric: true),
            'Z': new Figure(name: 'Z', cells: [13, 21, 22, 23, 31], color: [0.0f, 1.0f, 1.0f]),
            'V': new Figure(name: 'V', cells: [11, 12, 13, 21, 31], color: [0.0f, 1.0f, 0.7f], symmetric: true),
            '4': new Figure(name: '4', cells: [13, 14, 21, 22, 23], color: [0.7f, 0.0f, 0.0f]),
            'C': new Figure(name: 'C', cells: [11, 12, 13, 21, 23], color: [0.0f, 0.7f, 0.0f], symmetric: true),
            'Y': new Figure(name: 'Y', cells: [21, 22, 23, 31, 12], color: [0.7f, 0.7f, 0.7f])
    ]

    Map<String, List<String>> allTypes =
            ['A': ['L', '1', 'T', 'P', 'W', 'Z', 'V', '4'],
             'B': ['4', 'P', 'C', 'L', 'Z', '1', 'T', 'W'],
             'C': ['L', 'V', 'P', '1', '4', 'C', 'Z', 'Y'],
             'D': ['1', 'P', 'C', '4', 'V', 'Y', 'W', 'T'],
             'E': ['L', '4', 'V', 'Z', 'C', 'T', '1', 'W'],
             'F': ['P', 'C', 'Y', '1', 'T', '4', 'L', 'W'],
             'G': ['L', 'V', 'P', 'Z', '1', 'W', '4', 'Y']]

    boolean printPlace = false

    //3-12
    int level = 3
    //A-G
    String type = 'A'
    Field field
    List<Figure> figures = []


    void init() {
        field = new Field(width: level)
        figures = allTypes[type].subList(0, level).collect { allFigures[it] }
    }

    boolean solve() {
        boolean ret = false
        init()
        println "Start solving..."
        ret = doSolve(figures)

        if(ret) {
            println "Solved!"
        } else {
            println "Can't solve :-("
        }
    }

    private boolean doSolve(List<Figure> figures) {
        boolean ret = false
        for (Figure figure : figures) {
            List<Figure> otherFigures = figures.clone()
            otherFigures.remove(figure)
            def freeSlots = field.freeLeftCells()
            def iter = freeSlots.iterator()
            while(!ret && iter.hasNext()) {
                figure.reset()
                ret = doSolve(iter.next(), figure, otherFigures)
            }

            if(ret) {
                break
            }
        }
        ret
    }

    private boolean doSolve(Cell leftSlot, Figure figure, List<Figure> availableFigures) {
        boolean ret = false
        while(true) {
            ret = doSolvePosition(leftSlot, figure)
            if(ret && availableFigures) {
                ret = doSolve(availableFigures)
                if(!ret) {
                    field.take(figure)
                }
            }

            if(!ret && figure.hasNextPosition()) {
                figure.nextPosition()
            } else {
                break
            }
        }

        if(!ret) {
            figure.reset()
        }

        ret
    }

    private boolean doSolvePosition(Cell leftSlot, Figure figure) {
        boolean ret = false

        int h = leftSlot.h - figure.h.min.h
        int v = leftSlot.v - figure.h.min.v

        figure.move(h, v)

        if (field.fits(figure)) {
            field.place(figure)
            if(printPlace) {
                println "Place $figure"
                field.draw()
            }
            ret = true
        }

        ret
    }

    void showFigureMovements() {
        figures.each { def figure -> figure.draw(true) }
        println '### Invert'
        figures.each { def figure -> figure.invert() }
        figures.each { def figure -> figure.draw(true) }
        println '### Turn 1'
        figures.each { def figure -> figure.turn() }
        figures.each { def figure -> figure.draw(true) }
        println '### Turn 2'
        figures.each { def figure -> figure.turn() }
        figures.each { def figure -> figure.draw(true) }
        println '### Turn 3'
        figures.each { def figure -> figure.turn() }
        figures.each { def figure -> figure.draw(true) }
        println '### Turn 4'
        figures.each { def figure -> figure.turn() }
        figures.each { def figure -> figure.draw(true) }
        println '### Invert'
        figures.each { def figure -> figure.invert() }
        figures.each { def figure -> figure.draw(true) }

        figures.each { def figure -> figure.reset() }
    }

    void draw() {
        field.draw()
    }
}