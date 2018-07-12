from CPMITM import *
MC = [[1,0,1,1],[1,0,0,0],[0,1,1,0],[1,0,1,0]]

class SKINNY(Cipher):
    def genVars_input_of_round(self, r):
        assert r >= 1
        return ['u'+str(j)+'_r'+str(r) for j in range(0, self.wc)]

    def genVars_afterMC(self, r):
        return self.genVars_input_of_round(r + 1)

    def genVars_afeterSR(self, r):
        assert r >= 1
        return ['v'+str(j)+'_r'+str(r) for j in range(0, self.wc)]

    def genConstraints_of_Round(self, r):
        _X = BasicTools.typeX
        _Y = BasicTools.typeY
        _Z = BasicTools.typeZ

        U = self.genVars_input_of_round(r)
        V = self.genVars_afeterSR(r)
        nextU = self.genVars_afterMC(r)

        constr =[]

        # 1. Constraints of Foward Differential
        constr = constr + MITMConstraints.equalConstraints(_X(ShiftRow(U)), _X(V)) # - Constraints for S-box and ShiftRow
        for j in range(0, 4): # - Constraints for MixCols
            constr = constr + MITMConstraints.ForwardDiff_LinearLayer(MC, _X(column(V, j)), _X(column(nextU, j)))

        # 2. Constraints of Backward Determination
        constr = constr + MITMConstraints.equalConstraints(_Y(ShiftRow(U)), _Y(V))
        for j in range(0, 4): # - Constraints for MixCols
            constr = constr + MITMConstraints.BackwardDet_LinearLayer(MC, _Y(column(V, j)), _Y(column(nextU, j)))

        # 3. Constraints of the relationship of type X and type Y vars
        constr = constr + MITMConstraints.relationXYZ(_X(U), _Y(U), _Z(U))
        constr = constr + MITMConstraints.relationXYZ(_X(V), _Y(V), _Z(V))
        constr = constr + MITMConstraints.relationXYZ(_X(nextU), _Y(nextU), _Z(nextU))

        return constr

    def genObjectiveFun_to_Round(self, i):
        terms = []

        for j in range(2, i+1):
            terms = terms + self.genVars_input_of_round(j)

        return BasicTools.plusTerm(BasicTools.typeZ(terms))

    def genConstraints_Additional(self):
        A = BasicTools.typeX( self.genVars_input_of_round(1) )
        B = BasicTools.typeY( self.genVars_input_of_round(self.totalRounds + 1) )

        constr = []
        constr = constr + [BasicTools.plusTerm(A) + ' >= 1']
        constr = constr + [BasicTools.plusTerm(B) + ' >= 1']

        return constr

    def traceSol(self, f, r):
        F = SolFilePaser(f)

        print("Type Z:")
        for i in range(1, r+2):
            z = BasicTools.typeZ(self.genVars_input_of_round(i))
            prettyPrint(F.getBitPatternsFrom(z))

        print("\n\n")
        print("Type X:")
        for i in range(1, r+2):
            x = BasicTools.typeX(self.genVars_input_of_round(i))
            prettyPrint(F.getBitPatternsFrom(x))

        print("\n\n")
        print("Type Y:")
        for i in range(1, r+2):
            y = BasicTools.typeY(self.genVars_input_of_round(i))
            prettyPrint(F.getBitPatternsFrom(y))

def main():
    R = 10
    Skinny = SKINNY('SKINNY', 8, 128, 10)
    Skinny.genModel('R'+str(R)+'SKINNY.lp', R)
    Skinny.traceSol('R10SKINNY.sol', 10)


if __name__ == '__main__':
    main()
