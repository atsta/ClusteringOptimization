#include <iostream>
#include<fstream>
using namespace std;

const int NUM_VERTICES = 4000000;
const int NUM_EDGES = 117185084;
const int NUM_PARTITIONS = 4;
const int MAX_COM_VOLUME = 2 * NUM_EDGES/NUM_PARTITIONS;
int DEGREES[NUM_EDGES];

void do_degree_calculation(int u, int v)
{
    ++DEGREES[u];
    ++DEGREES[v];
}

void read()
{
    ifstream fin;
    string line;
    int u, v;
    fin.open("dataset.csv");
    while(!fin.eof())
    {
        fin>>line;
        cout << "edge u " << line; 
       // cout << "edge v " << line[2]; 

        // u = (int)line[0];
        // v = (int)line[2];

        // do_degree_calculation(u, v);
    }
}

void init_degrees() 
{ 
    for (int i = 0; i < NUM_VERTICES; i++) 
    {
        DEGREES[i] = 0;
    }
}

int main() 
{
    init_degrees();
    read();

    return 0;
}